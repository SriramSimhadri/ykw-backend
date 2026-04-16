package com.ykw.article.util;

import com.ykw.common.utility.UniqueIdGenerator;

public class ArticleUtil {

    public static String slugify(String title) {
        return replaceSpecialChars(title) + "-" + UniqueIdGenerator.generate();
    }

    public static String replaceSpecialChars(String input) {
        return input.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-");
    }

    public static String articleId() {
        return UniqueIdGenerator.generate();
    }
}
