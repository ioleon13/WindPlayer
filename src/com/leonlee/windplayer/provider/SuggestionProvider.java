package com.leonlee.windplayer.provider;

import android.content.SearchRecentSuggestionsProvider;

public class SuggestionProvider extends SearchRecentSuggestionsProvider {
    private String TAG = "SuggestionProvider";
    
    public final static String AUTHORITY = "com.leonlee.windplayer.provider.SuggestionProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;
    
    public SuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}
