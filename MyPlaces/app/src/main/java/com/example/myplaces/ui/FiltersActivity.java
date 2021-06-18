package com.example.myplaces.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Switch;

import com.example.myplaces.R;

public class FiltersActivity extends Activity implements AdapterView.OnItemSelectedListener {
    String animalType;
    Switch cfood ;
    Switch cmedicine;
    Switch cwater ;
    Switch cvet ;
    Switch cadoption ;
    Spinner type;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filters);
        cfood = findViewById(R.id.switchFilterFood);
        cmedicine = findViewById(R.id.switchFilterMedicine);
        cwater = findViewById(R.id.switchFilterWater);
        cvet = findViewById(R.id.switchFilterVet);
        cadoption = findViewById(R.id.switchFilterAdoption);
        type=findViewById(R.id.animaltype_spinnerFilter);
        // INICIJALNO PODESAVANJE FILTERA
        Intent listIntent = getIntent();
        Bundle positionBundle = listIntent.getExtras();
        cfood.setChecked(positionBundle.getBoolean("food"));
        cwater.setChecked(positionBundle.getBoolean("water"));
        cadoption.setChecked(positionBundle.getBoolean("adoption"));
        cmedicine.setChecked(positionBundle.getBoolean("medicine"));
        cvet.setChecked(positionBundle.getBoolean("vet"));
        findViewById(R.id.switchFilterAdoption);
        // SPINNER

        Spinner spinner = (Spinner) findViewById(R.id.animaltype_spinnerFilter);
        spinner.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.amimals_array_all, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setSelection(adapter.getPosition(positionBundle.getString("animalType")));
        //
        findViewById(R.id.btnCloseFilter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("food", cfood.isChecked());
                resultIntent.putExtra("medicine", cmedicine.isChecked());
                resultIntent.putExtra("water", cwater.isChecked());
                resultIntent.putExtra("vet", cvet.isChecked());
                resultIntent.putExtra("adoption", cadoption.isChecked());
                resultIntent.putExtra("animalType", animalType);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        animalType = parent.getItemAtPosition(pos).toString();
    }

    public void onNothingSelected(AdapterView<?> parent) {
        animalType = parent.getItemAtPosition(0).toString();
    }
}