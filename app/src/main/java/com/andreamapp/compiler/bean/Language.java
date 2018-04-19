package com.andreamapp.compiler.bean;

import android.content.Context;

import com.andreamapp.compiler.utils.LanguageParser;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Created by Andream on 2017/2/5.
 * Website: http://andream.com.cn
 * Email: me@andream.com.cn
 */

public class Language {

    private static Language[] LANGUAGES;

    public static void init(Context context) throws IOException {
        if(LANGUAGES == null){
            LANGUAGES = LanguageParser.parseAll(context);
        }
    }

    public static Language[] getLanguages(){
        return LANGUAGES;
    }

    private String name, code, suffix, template;
    private int templateCursorOffset;

    private String[] keepWords;
    private Pattern[] syntaxRegex;
    private int[] syntaxColors;

    public Language(String name, String code, String suffix, String template, int templateCursorOffset, String[] keepWords, Pattern[] syntaxRegex, int[] syntaxColors) {
        this.name = name;
        this.code = code;
        this.suffix = suffix;
        this.template = template;
        this.templateCursorOffset = templateCursorOffset;
        this.keepWords = keepWords;
        this.syntaxRegex = syntaxRegex;
        this.syntaxColors = syntaxColors;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public String getSuffix() {
        return suffix;
    }

    public String getTemplate() {
        return template;
    }

    public int getTemplateCursorOffset() {
        return templateCursorOffset;
    }

    public String[] getKeepWords() {
        return keepWords;
    }

    public Pattern[] getSyntaxRegex() {
        return syntaxRegex;
    }

    public int[] getSyntaxColors() {
        return syntaxColors;
    }
}
