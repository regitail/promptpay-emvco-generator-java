/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.regitail;

import com.github.snksoft.crc.CRC;
import com.regitail.exceptions.NegativeAmountException;
import com.regitail.exceptions.TargetMismatchException;
import net.glxn.qrgen.core.image.ImageType;
import net.glxn.qrgen.javase.QRCode;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class provide Thailand's PromptPay EMVCo String
 * <p>
 * Example
 * <pre>
 * {@code
 *     String target = "0812345678";
 *     Double amount = "15000.00";
 *     PromptPayEMVCoGenerator promptpay = new PromptPayEMVCoGenerator(target, amount);
 *     String emvco = promptpay.generate();
 *     System.out.println("My PromptPay String: " + emvco);
 * }
 * </pre>
 */
public class PromptPayEMVCoGenerator {
    private final String F_01_VERSION = "000201";
    private final String F_02_QR_TYPE = "010212";
    private final String F_03_MERCHANT_INFO = "29370016A000000677010111${accountType}13${accountNumber}";
    private final String F_04_COUNTRY_CODE = "5802TH";
    private final String F_05_CURRENCY_CODE = "5303764";
    private final String F_06_AMOUNT = "54${digitCode}${amount}";
    private final String F_07_CHECKSUM = "6304${checksumHex}";
    private final String C_CITIZEN = "02";
    private final String C_TELEPHONE = "01";
    private final String C_PHONE_PREFIX = "0066";
    private final int C_QR_IMAGE_DEFAULT_SIZE = 250;

    private final CRC crcGenertor = new CRC(CRC.Parameters.CCITT);

    private String accountType, accountNumber, digitCode, amount;

    /**
     * EMVCo generator for Thailand PromptPay
     * @param target target account
     * @param amount money to transfer
     * @throws TargetMismatchException is will be throw when target account format is mismatched
     * @throws NegativeAmountException will be throw when amount is a negative value
     */
    public PromptPayEMVCoGenerator (String target, Double amount) throws TargetMismatchException, NegativeAmountException {
        this.accountType = this.accountTypeCheck(target);

        if (C_CITIZEN.equals(this.accountType)) {
            this.accountNumber = target;
        } else {
            this.accountNumber = this.convertToProperPhoneNo(target);
        }

        if (amount < 0) {
            throw new NegativeAmountException();
        }

        amount = Math.round(amount * 100.0) / 100.0;
        this.amount = amount.toString();

        this.digitCode = String.format("%02d", this.amount.length());
    }

    /**
     * EMVCo generator for Thailand PromptPay
     * @return EMVCo string
     */
    public String generate () {
        String emvco = "";
        emvco += F_01_VERSION;
        emvco += F_02_QR_TYPE;
        emvco += F_03_MERCHANT_INFO.replace("${accountType}", this.accountType)
                .replace("${accountNumber}", this.accountNumber);
        emvco += F_04_COUNTRY_CODE;
        emvco += F_05_CURRENCY_CODE;
        emvco += F_06_AMOUNT.replace("${digitCode}", this.digitCode).replace("${amount}", this.amount);
        emvco += F_07_CHECKSUM.replace("${checksumHex}", "");

        // Generate CRC16 checks sum
        String checksum = String.format("%04X", crcGenertor.calculateCRC(emvco.getBytes())).toUpperCase();
        emvco += checksum;

        return emvco;
    }

    /**
     * PromptPay QR Code Generator
     * @param filename QR code image file name
     * @param imageType Image type see: net.glxn.qrgen.core.image.ImageType
     * @return File QR code image file
     */
    public File generateQRCode (String filename, ImageType imageType) {
        return this.generateQRCode(filename, imageType, this.C_QR_IMAGE_DEFAULT_SIZE);
    }

    /**
     * PromptPay QR Code Generator
     * @param filename QR code image file name
     * @param imageType Image type see: net.glxn.qrgen.core.image.ImageType
     * @param imageSize Image size in pixel
     * @return File QR code image file
     */
    public File generateQRCode (String filename, ImageType imageType, int imageSize) {
        String emvco = this.generate();
        ImageType iType = ImageType.PNG;

        if (imageType != null) {
            iType = imageType;
        }

        File f = QRCode.from(emvco).withSize(imageSize, imageSize).to(iType).file();
        try {
            Files.copy(f.toPath(), Paths.get(filename), StandardCopyOption.REPLACE_EXISTING);
            f = new File(filename);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return f;
    }

    /**
     * Proper telephone number converter
     * @param originalPhoneNo original phone number
     * @return proper format like, 0066812345678
     */
    private String convertToProperPhoneNo (String originalPhoneNo) {
        String newPhoneNo = originalPhoneNo.substring(1);
        newPhoneNo = C_PHONE_PREFIX + newPhoneNo;

        return newPhoneNo;
    }

    /**
     * Account number type checker
     * @param accountTarget account target
     * @return 01: Telephone, 02: Citizen ID
     * @throws TargetMismatchException format mismatch
     */
    private String accountTypeCheck (String accountTarget) throws TargetMismatchException {
        String accType = "00";

        if (isCitizenNumber(accountTarget)) {
            accType = "02";
        } else if (isPhoneNumber(accountTarget)) {
            accType = "01";
        } else {
            throw new TargetMismatchException();
        }

        return accType;
    }

    /**
     * Check is Thailand Citizen ID
     * @param accountNumber account number
     * @return true: if it is citizen number
     */
    private boolean isCitizenNumber (String accountNumber) {
        String patter = "^\\d{13}$";
        Pattern p = Pattern.compile(patter);
        Matcher m = p.matcher(accountNumber);

        return m.find();
    }

    /**
     * Check is Thailand phone number
     * @param accountNumber account number
     * @return true: if it is phone number
     */
    private boolean isPhoneNumber (String accountNumber) {
        String patter = "^\\d{10}$";
        Pattern p = Pattern.compile(patter);
        Matcher m = p.matcher(accountNumber);

        return m.find();
    }
}
