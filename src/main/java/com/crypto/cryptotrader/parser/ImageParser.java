package com.crypto.cryptotrader.parser;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.TesseractException;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.crypto.cryptotrader.exchange.CurrencyCodesHelper;

@Component
public class ImageParser {

	@Autowired
	private ITesseract tesseract;
	@Autowired
	private CurrencyCodesHelper currencyCodesHelper;

	public String getCurrencyCodeFromImage(String imageUrl) throws TesseractException, IOException {
		BufferedImage bufferedImage = ImageIO.read(new URL(imageUrl));
		String imageText = tesseract.doOCR(bufferedImage);
		Map<String, String> currencyCodesMap = currencyCodesHelper.getCurrencyCodesMap();
		String currencyCode = TextParserUtils.getCurrencyCodeFromMessage(imageText, currencyCodesMap);

		return currencyCode;
	}

}
