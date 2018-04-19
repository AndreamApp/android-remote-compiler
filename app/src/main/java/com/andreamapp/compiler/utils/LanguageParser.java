package com.andreamapp.compiler.utils;

import android.content.Context;
import android.graphics.Color;

import com.andreamapp.compiler.bean.Language;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Created by Andream on 2017/2/7.
 * Website: http://andream.com.cn
 * Email: me@andream.com.cn
 */

public class LanguageParser {

    public static Language[] parseAll(Context context) throws IOException {
        return parseAll(context.getAssets().open("language.in"));
    }

    public static Language[] parseAll(InputStream is) {
        Scanner in = new Scanner(is);
        int T = in.nextInt();
        Language[] languages = new Language[T];
        for (int i = 0; i < T; i++) {
            languages[i] = parse(in);
        }
        return languages;
    }

    private static Language parse(Scanner in) {
        String name = in.next();
        String code = in.next();
        String suffix = in.next();

        //Template
        int templateLines = in.nextInt();
        int templateCursorOffset = in.nextInt();
        in.nextLine();
        StringBuilder templateBuilder = new StringBuilder();
        for (int i = 0; i < templateLines; i++) {
            templateBuilder.append(in.nextLine()).append("\n");
        }
        String template = templateBuilder.toString();

        // reserve words
        int reserveWordsCounts = in.nextInt();
        String[] reserveWords = new String[reserveWordsCounts];
        for (int i = 0; i < reserveWordsCounts; i++) {
            reserveWords[i] = in.next();
        }
        int reserveWordColor = Color.parseColor("#" + in.next());

        // syntax regex & colors
        int syntaxCounts = in.nextInt();
        Pattern[] syntaxRegex = new Pattern[reserveWordsCounts + syntaxCounts];
        int[] syntaxColors = new int[reserveWordsCounts + syntaxCounts];

        for (int i = 0; i < reserveWordsCounts; i++) {
            syntaxRegex[i] = Pattern.compile("(?=\\b)"+reserveWords[i]+"(?<=\\b)");
            syntaxColors[i] = reserveWordColor;
        }
        for (int i = reserveWordsCounts; i < reserveWordsCounts + syntaxCounts; i++) {
            syntaxRegex[i] = Pattern.compile(in.next());
            syntaxColors[i] = Color.parseColor("#"+in.next());
        }

        Language lang = new Language(name, code, suffix, template, templateCursorOffset, reserveWords, syntaxRegex, syntaxColors);
        return lang;
    }
}
