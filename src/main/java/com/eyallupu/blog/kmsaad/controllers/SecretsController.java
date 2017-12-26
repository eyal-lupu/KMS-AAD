package com.eyallupu.blog.kmsaad.controllers;

import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.model.CreateAliasRequest;
import com.amazonaws.services.kms.model.CreateKeyRequest;
import com.amazonaws.services.kms.model.CreateKeyResult;
import com.amazonaws.services.kms.model.DecryptRequest;
import com.amazonaws.services.kms.model.DecryptResult;
import com.amazonaws.services.kms.model.DescribeKeyRequest;
import com.amazonaws.services.kms.model.GenerateDataKeyRequest;
import com.amazonaws.services.kms.model.GenerateDataKeyResult;
import com.amazonaws.services.kms.model.NotFoundException;

@RestController
public class SecretsController {

	@Autowired
	private AWSKMS awskms;

	/**
	 * Alias for the CMK - in practice this can be any key-id (e.g. ARN as well) but
	 * to make things easier for this example I assume an alias is used (see
	 * {@link #validateMasterKey()})
	 */
	@Value("${SecretsController.cmkAlias}")
	private String cmkAlias;

	/**
	 * This map will store the secrets - a mapping from secret name to an
	 * {@link Envelope}.
	 * 
	 * NB: In practice I should have used the user name as a pseudo key of that map
	 * (or a part of a composed key) but this would have make the example even more
	 * complicated.
	 */
	private Map<String, Envelope> secrets = new HashMap<>();

	@RequestMapping(path = "/secrets/{name}", method = RequestMethod.POST, consumes = "text/plain")
	public ResponseEntity<String> put(@PathVariable("name") String name, @RequestBody String value, Principal principal)
			throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException,
			BadPaddingException, InvalidAlgorithmParameterException {

		// Must have a name and value
		if (null == value || null == name) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		// Encrypt

		// We start by generating a data key
		GenerateDataKeyRequest dataKeyRequest = new GenerateDataKeyRequest().withKeyId(cmkAlias).withKeySpec("AES_128");
		GenerateDataKeyResult dataKeyResult = awskms.generateDataKey(dataKeyRequest);

		// The data key is just raw material - build a JCE key for Java to use
		Key key = buildJCEKey(dataKeyResult.getPlaintext().asReadOnlyBuffer());
		dataKeyResult.getPlaintext().clear(); // Clear it ASAP!!

		// Now encrypt - usual Java encryption and store as an envelope containing:
		// - The data key in the encrypted form (needs the master key to open)
		// - The encrypted payload
		String encrypted = encrypt(value, key);
		Envelope envelope = new Envelope(dataKeyResult.getCiphertextBlob(), encrypted);

		// Store in map - probably in real life will go to a less volatile storage ...
		if (null == secrets.put(name, envelope)) {
			return new ResponseEntity<>(HttpStatus.CREATED);
		} else {
			return new ResponseEntity<>(HttpStatus.OK);
		}
	}

	@RequestMapping(path = "/secrets/{name}", method = RequestMethod.GET)
	public ResponseEntity<String> get(@PathVariable("name") String name, Principal principal)
			throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException,
			BadPaddingException, InvalidAlgorithmParameterException {

		// NAme of the secret is a must
		if (null == name) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		Envelope envelope = secrets.get(name);
		if (null == envelope) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} else {
			// Get the encrypted data key and open using the CMK
			DecryptRequest decryptRequest = new DecryptRequest().withCiphertextBlob(envelope.key);
			DecryptResult decryptResult = awskms.decrypt(decryptRequest);

			// Build a JCE Key out of it - for Java to use
			Key key = buildJCEKey(decryptResult.getPlaintext().asReadOnlyBuffer());
			decryptResult.getPlaintext().clear();

			// Decrypt the actual secret
			String cleartext = decrypt(envelope.payload, key);

			return new ResponseEntity<>(cleartext, HttpStatus.OK);
		}
	}

	/**
	 * Validates that the required master key (CMK) exists. This is just an init
	 * method to make sure I have the CMK ready for me.
	 * 
	 * To keep it easier I am using alias and not an ARN
	 */
	@PostConstruct
	public void validateMasterKey() throws Exception {
		DescribeKeyRequest describeKeyRequest = new DescribeKeyRequest().withKeyId(cmkAlias);
		try {
			awskms.describeKey(describeKeyRequest);
		} catch (NotFoundException nfe) {
			// No key - create
			CreateKeyRequest createKeyRequest = new CreateKeyRequest();
			CreateKeyResult createKey = awskms.createKey(createKeyRequest);
			awskms.createAlias(new CreateAliasRequest().withAliasName(cmkAlias)
					.withTargetKeyId(createKey.getKeyMetadata().getKeyId()));
		}
	}

	private String encrypt(String cleartext, Key key) throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {

		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.ENCRYPT_MODE, key);

		byte[] enc = cipher.doFinal(cleartext.getBytes());
		return Base64.getEncoder().encodeToString(enc);
	}

	private String decrypt(String ciphertext, Key key) throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {

		byte[] decodeBase64src = Base64.getDecoder().decode(ciphertext);

		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE, key);
		return new String(cipher.doFinal(decodeBase64src));
	}

	private Key buildJCEKey(ByteBuffer raw) {
		byte[] arr = new byte[raw.remaining()];
		raw.get(arr);
		return new SecretKeySpec(arr, "AES");
	}

	private static class Envelope {

		/**
		 * The encryption data (secondary) key encrypted by the CMK
		 */
		final ByteBuffer key;

		/**
		 * The encrypted form
		 */
		final String payload;

		public Envelope(ByteBuffer key, String payload) {
			this.key = key;
			this.payload = payload;
		}

	}
}