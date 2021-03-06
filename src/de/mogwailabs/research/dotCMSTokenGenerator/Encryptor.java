/**
 * Copyright (c) 2000-2005 Liferay, LLC. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.mogwailabs.research.dotCMSTokenGenerator;

//import java.io.ObjectInputFilter.Config;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;

import com.liferay.util.EncryptorException;

/**
 * <a href="Encryptor.java.html"><b><i>View Source</i></b></a>
 *
 * @author  Brian Wing Shun Chan
 * @version $Revision: 1.13 $
 *
 */
public class Encryptor {

	public static final String ENCODING = "UTF8";

	public static final String DIGEST_ALGORITHM = "SHA256";

	public static final String KEY_ALGORITHM = "AES";
    public static final int KEY_LENGTH = 256;
	
	public static Key generateKey() throws EncryptorException {
		return generateKey(KEY_ALGORITHM);
	}

	public static Key generateKey(final String algorithm) throws EncryptorException {
      final KeyGenerator kgen;
      try {
          kgen = KeyGenerator.getInstance(algorithm);
      } catch (final NoSuchAlgorithmException e) {
          throw new RuntimeException(algorithm + " key generator should always be available in a Java runtime", e);
      }
      final SecureRandom rng;
      try {
          rng = SecureRandom.getInstanceStrong();
      } catch (final NoSuchAlgorithmException e) {
          throw new RuntimeException("No strong secure random available to generate strong key", e);
      }
      // already throws IllegalParameterException for wrong key sizes
      kgen.init(KEY_LENGTH, rng);

      return kgen.generateKey();
	}

	

	public static String decrypt(Key key, String encryptedString)
		throws EncryptorException {

		try {

			Cipher cipher = Cipher.getInstance(key.getAlgorithm());
			cipher.init(Cipher.DECRYPT_MODE, key);

			byte[] encryptedBytes = Base64.decode(encryptedString);
			byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

			String decryptedString = new String(decryptedBytes, ENCODING);

			return decryptedString;
		}
		catch (Exception e) {
			throw new EncryptorException(e);
		}
	}

	public static String digest(String text) {
		return digest(DIGEST_ALGORITHM, text);
	}

	public static String digest(String algorithm, String text) {
		MessageDigest mDigest = null;

		try{
			mDigest = MessageDigest.getInstance(algorithm);

			mDigest.update(text.getBytes(ENCODING));
		}
		catch(NoSuchAlgorithmException nsae) {
			//Logger.error(Encryptor.class,nsae.getMessage(),nsae);
			
		}
		catch(UnsupportedEncodingException uee) {
			//Logger.error(Encryptor.class,uee.getMessage(),uee);
		}

		byte raw[] = mDigest.digest();

		return Base64.encode(raw);
	}
	public static String encrypt(Key key, String plainText) throws Exception {

		Cipher cipher = Cipher.getInstance(key.getAlgorithm());
		cipher.init(Cipher.ENCRYPT_MODE, key);

		byte[] decryptedBytes = plainText.getBytes("UTF8");
		byte[] encryptedBytes = cipher.doFinal(decryptedBytes);

		String encryptedString = Base64.encode(encryptedBytes);

		return encryptedString;

}
	

}