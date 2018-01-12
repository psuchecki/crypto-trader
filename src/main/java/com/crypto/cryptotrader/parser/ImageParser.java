package com.crypto.cryptotrader.parser;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.TesseractException;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.crypto.cryptotrader.exchange.CurrencyCodesHelper;

@Component
public class ImageParser {
	public static final String CURRENCY_CODE_REGEX = "\\(\\w+\\)";
	public static final Pattern CURRENCY_CODE_PATTERN = Pattern.compile(CURRENCY_CODE_REGEX);

	@Autowired
	private ITesseract tesseract;
	@Autowired
	private CurrencyCodesHelper currencyCodesHelper;

	public String getCurrencyCodeFromImage(String imageUrl) throws TesseractException, IOException {
		Map<String, String> currencyCodesMap = currencyCodesHelper.getCurrencyCodesMap();
		BufferedImage bufferedImage = ImageIO.read(new URL(imageUrl));
		String imageText = tesseract.doOCR(bufferedImage);
		Matcher matcher = CURRENCY_CODE_PATTERN.matcher(imageText);

		while (matcher.find()) {
			String currencyCode = matcher.group(matcher.groupCount());
			currencyCode = TextParserUtils.extractCurrencyCode(currencyCode);
			if (currencyCodesMap.get(currencyCode) != null) {
				return currencyCode;
			}
		}

		return null;
	}

}
