package com.example.assignment;
import static android.app.ProgressDialog.show;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Stack; // Import Stack

public class MainActivity extends AppCompatActivity {

    TextView text, size;
    Button addtext;
    ImageButton undo, redo, plus, minus, boldText, italicText, underLine;

    float dX, dY;
    float text_size_variable; // Renamed to avoid confusion with TextView size property

    // Stacks for Undo/Redo
    private Stack<TextViewState> undoStack = new Stack<>();
    private Stack<TextViewState> redoStack = new Stack<>();

    // Flags for current style state (important for toggle logic)
    private boolean currentBold = false;
    private boolean currentItalic = false;
    private boolean currentUnderline = false;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        text = findViewById(R.id.text);
        addtext = findViewById(R.id.Addtext); // Assuming you'll implement text changes here
        undo = findViewById(R.id.undo);
        redo = findViewById(R.id.redo);
        plus = findViewById(R.id.plus);
        minus = findViewById(R.id.minus);
        boldText = findViewById(R.id.boldText);
        italicText = findViewById(R.id.italicText);
        underLine = findViewById(R.id.underLine);
        size = findViewById(R.id.size);

        // Initial state
        text_size_variable = 30; // Use the renamed variable
        text.setTextSize(text_size_variable);
        size.setText(String.valueOf(text_size_variable));
        text.setText("Hello World"); // Initial text
        saveCurrentStateForUndo(null); // Save initial state, pass null as "action before this state"

        //setText
        addtext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Dialog dialog = new Dialog(MainActivity.this);
                dialog.setContentView(R.layout.add_text);
                dialog.show();
                EditText editText = dialog.findViewById(R.id.Add_New_Text);
                Button saveBtn = dialog.findViewById(R.id.saveBtn);
                saveBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String newText = editText.getText().toString();
                        if(newText.isEmpty()){
                            Toast.makeText(MainActivity.this, "Enter Text", Toast.LENGTH_SHORT).show();
                        }else{
                            text.setText(newText);
                            dialog.dismiss();
                        }
                    }
                });

            }
        });


        text.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        dX = v.getX() - event.getRawX();
                        dY = v.getY() - event.getRawY();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        v.animate()
                                .x(event.getRawX() + dX)
                                .y(event.getRawY() + dY)
                                .setDuration(0)
                                .start();
                        return true;

                    case MotionEvent.ACTION_UP:
                        return true;

                    default:
                        return false;
                }
            }
        });


        //undo
        undo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performUndo();
            }
        });

        //redo
        redo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performRedo();
            }
        });

        //text_Size_Increase
        plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCurrentStateForUndo("sizeIncreased");
                text_size_variable += 1;
                applyState(new TextViewState(text.getText().toString(), text_size_variable, currentBold, currentItalic, currentUnderline, getTypefaceStyle()));
                redoStack.clear();
                updateUndoRedoButtonStates();
            }
        });

        //text_Size_Decrease
        minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (text_size_variable > 1) { // Prevent going below 1
                    saveCurrentStateForUndo("sizeDecreased");
                    text_size_variable -= 1;
                    applyState(new TextViewState(text.getText().toString(), text_size_variable, currentBold, currentItalic, currentUnderline, getTypefaceStyle()));
                    redoStack.clear();
                    updateUndoRedoButtonStates();
                }
            }
        });

        boldText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCurrentStateForUndo("boldToggled");
                currentBold = !currentBold; // Toggle state
                applyState(new TextViewState(text.getText().toString(), text_size_variable, currentBold, currentItalic, currentUnderline, getTypefaceStyle()));
                redoStack.clear();
                updateUndoRedoButtonStates();
            }
        });

        italicText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCurrentStateForUndo("italicToggled");
                currentItalic = !currentItalic; // Toggle state
                applyState(new TextViewState(text.getText().toString(), text_size_variable, currentBold, currentItalic, currentUnderline, getTypefaceStyle()));
                redoStack.clear();
                updateUndoRedoButtonStates();
                Toast.makeText(MainActivity.this, currentItalic ? "Text_ITALIC" : "Text_NORMAL (Italic Off)", Toast.LENGTH_SHORT).show();
            }
        });

        underLine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCurrentStateForUndo("underlineToggled");
                currentUnderline = !currentUnderline; // Toggle state
                applyState(new TextViewState(text.getText().toString(), text_size_variable, currentBold, currentItalic, currentUnderline, getTypefaceStyle()));
                redoStack.clear();
                updateUndoRedoButtonStates();
                Toast.makeText(MainActivity.this, currentUnderline ? "Text_UNDERLINE" : "Text_NORMAL (Underline Off)", Toast.LENGTH_SHORT).show();
            }
        });
        updateUndoRedoButtonStates(); // Initial button states
    }

    private int getTypefaceStyle() {
        int style = Typeface.NORMAL;
        if (currentBold && currentItalic) {
            style = Typeface.BOLD_ITALIC;
        } else if (currentBold) {
            style = Typeface.BOLD;
        } else if (currentItalic) {
            style = Typeface.ITALIC;
        }
        return style;
    }

    // Method to capture the current state of the TextView before an action
    private void saveCurrentStateForUndo(String actionDescription) {
        TextViewState currentState = new TextViewState(
                text.getText().toString(),
                text_size_variable,
                currentBold,
                currentItalic,
                currentUnderline,
                getTypefaceStyle()
        );
        undoStack.push(currentState);
    }


    private void applyState(TextViewState state) {
        if (state == null) return;

        text.setText(state.textContent);
        text.setTextSize(state.textSize);

        currentBold = state.isBold;
        currentItalic = state.isItalic;
        currentUnderline = state.isUnderlined;
        text_size_variable = state.textSize;

        text.setTypeface(null, state.typefaceStyle);

        if (state.isUnderlined) {
            text.setPaintFlags(text.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        } else {
            text.setPaintFlags(text.getPaintFlags() & (~Paint.UNDERLINE_TEXT_FLAG));
        }

        size.setText(String.valueOf(state.textSize)); // Update the size display
    }


    private void performUndo() {
        if (!undoStack.isEmpty()) {
            // Before undoing, save the current state to the redo stack
            TextViewState currentStateForRedo = new TextViewState(
                    text.getText().toString(),
                    text_size_variable,
                    currentBold,
                    currentItalic,
                    currentUnderline,
                    getTypefaceStyle()
            );
            redoStack.push(currentStateForRedo);


            TextViewState stateToApply = undoStack.pop();
            applyState(stateToApply);

            Toast.makeText(this, "Undo", Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(this, "Nothing to undo", Toast.LENGTH_SHORT).show();
        }
        updateUndoRedoButtonStates();
    }

    private void performRedo() {
        if (!redoStack.isEmpty()) {

            TextViewState currentStateForUndo = new TextViewState(
                    text.getText().toString(),
                    text_size_variable,
                    currentBold,
                    currentItalic,
                    currentUnderline,
                    getTypefaceStyle()
            );
            undoStack.push(currentStateForUndo);


            TextViewState stateToApply = redoStack.pop();
            applyState(stateToApply);

            Toast.makeText(this, "Redo", Toast.LENGTH_SHORT).show();


        } else {
            Toast.makeText(this, "Nothing to redo", Toast.LENGTH_SHORT).show();
        }
        updateUndoRedoButtonStates();
    }


    private void updateUndoRedoButtonStates() {
        undo.setEnabled(!undoStack.isEmpty());
        redo.setEnabled(!redoStack.isEmpty());
        // You can also change alpha, icons, etc.
        undo.setAlpha(undoStack.isEmpty() ? 0.5f : 1.0f);
        redo.setAlpha(redoStack.isEmpty() ? 0.5f : 1.0f);
    }
}
