package com.example.assignment;

public class TextViewState {
    public String textContent;
    public float textSize;
    public boolean isBold;
    public boolean isItalic;
    public boolean isUnderlined;
   public int typefaceStyle;

    public TextViewState(String textContent, float textSize, boolean isBold, boolean isItalic, boolean isUnderlined, int typefaceStyle) {
        this.textContent = textContent;
        this.textSize = textSize;
        this.isBold = isBold;
        this.isItalic = isItalic;
        this.isUnderlined = isUnderlined;
        this.typefaceStyle = typefaceStyle;
    }


}
