package com.example.simpletodo;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String KEY_ITEM_TEXT = "item text";
    public static final String KEY_ITEM_POSITION = "item position";
    private static final int EDIT_TEXT_CODE = 20;

    List<String> items;

    Button btnAdd;
    EditText etItem;
    RecyclerView rvItems;

    ItemsAdapter itemsAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnAdd = findViewById(R.id.btnAdd);
        etItem = findViewById(R.id.etItem);
        rvItems = findViewById(R.id.rvItems);

        //gets previous to-do items and puts them in items
        loadItems();

        //runs when the adapter is long clicked, deletes the long clicked item
        ItemsAdapter.OnLongClickListener onLongClickListener = new ItemsAdapter.OnLongClickListener() {
            @Override
            public void onItemLongClicked(int position) {
                //delete the item from the model
                items.remove(position);

                //notify the adapter that this item has just been removed
                itemsAdapter.notifyItemRemoved(position);
                Toast.makeText(getApplicationContext(), "Item was removed!", Toast.LENGTH_SHORT).show();
                //save changes to file
                saveItems();
            }
        };

        ItemsAdapter.OnClickListener onClickListener = new ItemsAdapter.OnClickListener() {
            @Override
            public void onItemClicked(int position) {

                // create the new activity
                Intent i = new Intent(MainActivity.this, EditActivity.class);
                // pass the data being edited
                i.putExtra(KEY_ITEM_TEXT, items.get(position));
                i.putExtra(KEY_ITEM_POSITION, position);
                // display the activity
                startActivityForResult(i, EDIT_TEXT_CODE);
                
            }
        };

        itemsAdapter = new ItemsAdapter(items, onLongClickListener, onClickListener);
        rvItems.setAdapter(itemsAdapter);
        rvItems.setLayoutManager(new LinearLayoutManager(this));

        //runs when btnAdd is clicked to add the to-do to the list
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String todoItem = etItem.getText().toString();
                //add item to the model
                items.add(todoItem);
                //notify adapter that an item has been inserted
                itemsAdapter.notifyItemInserted(items.size() - 1);

                //clear the edit text
                etItem.setText("");

                Toast.makeText(getApplicationContext(), "Item was added!", Toast.LENGTH_SHORT).show();

                //updates file system
                saveItems();
            }
        });

    }

    // handle the result of the edit activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && requestCode == EDIT_TEXT_CODE) {
            // Retrieve the updated text value
            String itemText = data.getStringExtra(KEY_ITEM_TEXT);

            //extract the original position of the edited item from the key position
            int position = data.getExtras().getInt(KEY_ITEM_POSITION);

            // update the model with the new item text
            items.set(position, itemText);

            //notify the adapter
            itemsAdapter.notifyItemChanged(position);

            //persist the changes
            saveItems();

            //indicate to the user that they have updated the item
            Toast.makeText(getApplicationContext(), "Item updated successfully!", Toast.LENGTH_SHORT).show();

        } else {
            Log.w("MainActivity", "Unknown call to onActivityResult");

        }
    }

    private File getDataFile() {
        return new File(getFilesDir(), "data.txt");
    }

    //This function will load items by reading every line of the data file
    private void loadItems(){
        try {
            items = new ArrayList<>(FileUtils.readLines(getDataFile(), Charset.defaultCharset()));
        } catch (IOException e) {
            Log.e("MainActivity", "error reading items", e);
            items = new ArrayList<>();
        }
    }

    //This function saves items by writing them into the data file
    private void saveItems(){
        try {
            FileUtils.writeLines(getDataFile(), items);
        } catch (IOException e) {
            Log.e("MainActivity", "error reading items", e);
        }
    }

}