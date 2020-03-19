package com.example.medilist;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.medilist.doctor.DocterUser;
import com.example.medilist.patient.BasicPatientActivity;
import com.example.medilist.patient.PatientUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Calendar;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class SignupAsPatientActivity extends AppCompatActivity {
    Calendar c;
    DatePickerDialog dpd;
    RadioGroup radioGenGroup;
    RadioButton radioGenButton;
    EditText nameEt,phnoEt,emailEt,passwordEt,conpasswordEt;
    TextView login,showDOB;
    Button btnDOB,signup;
    CircleImageView ProfileImage;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    String ID;
    FirebaseAuth auth;
    DatabaseReference dbr;
    ProgressDialog progressDialog;
    Uri resultUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_as_patient);
        auth = FirebaseAuth.getInstance();
        dbr= FirebaseDatabase.getInstance().getReference("Patient");
        compoAssign();
        clickSignup();
        clickLogin();
        clickDOB();
        choosepic();
    }
    public void compoAssign(){
        nameEt = (EditText)findViewById(R.id.ptPatName);
        phnoEt= (EditText)findViewById(R.id.ptPatPhNo);
        emailEt = findViewById(R.id.ptPatEmail);
        passwordEt = findViewById(R.id.ptPatPass);
        conpasswordEt = findViewById(R.id.ptPatCnfPass);
        signup = findViewById(R.id.btnPatSignup);
        login = findViewById(R.id.tvPatLogin);
        showDOB=findViewById(R.id.tvDOBView);
        btnDOB=findViewById(R.id.btnPatDob);
        login=findViewById(R.id.tvPatLogin);
        progressDialog = new ProgressDialog(SignupAsPatientActivity.this);
        ProfileImage = (CircleImageView) findViewById(R.id.btnPatProfilePic);
        radioGenGroup = (RadioGroup) findViewById(R.id.rgrpGender);

    }
    public void choosepic(){
        ProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).start(SignupAsPatientActivity.this);
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();
                ProfileImage.setImageURI(null);
                ProfileImage.setImageURI(resultUri);


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }



    public void clickDOB(){
        btnDOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                c=Calendar.getInstance();
                int day=c.get(Calendar.DAY_OF_MONTH);
                final int month=c.get(Calendar.MONTH);
                int year=c.get(Calendar.YEAR);

                dpd= new DatePickerDialog(SignupAsPatientActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int fyear, int fmonth, int fdayOfMonth) {
                        fmonth=fmonth+1;
                        showDOB.setText(fdayOfMonth + "/" + fmonth + "/" + fyear);
                    }
                },year,month,day);
                dpd.show();
            }
        });
    }
    private boolean validate(){
        boolean result = false;
        String emailS = emailEt.getText().toString();
        String passwordS= passwordEt.getText().toString();
        String conpasswordS = conpasswordEt.getText().toString();
        if(emailS.equals("") || !emailS.matches(emailPattern)){
            emailEt.setError("Enter valid email");
            emailEt.requestFocus();
        }else if(passwordS.equals("")|| passwordS.length()<6){
            passwordEt.setError("Password must be >6 letter");
            passwordEt.requestFocus();
        }else if(conpasswordS.equals("") || passwordS.length()<6){
            conpasswordEt.setError("Password must be >6 letter");
            conpasswordEt.requestFocus();
        }else if(!conpasswordS.equals(passwordS)){
            conpasswordEt.setError("Password doesn't match");
            conpasswordEt.requestFocus();
        }
        else{
            result = true;
        }
        return result;
    }
    public void clickLogin(){
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignupAsPatientActivity.this,LoginActivity.class));
            }
        });
    }
    public void clickSignup() {
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validate()){
                    showProgDialog();
                    String user_email = emailEt.getText().toString().trim();
                    String user_password = passwordEt.getText().toString().trim();
                    auth.createUserWithEmailAndPassword(user_email,user_password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(SignupAsPatientActivity.this, "Registration Complete", Toast.LENGTH_SHORT).show();
                                adduser();
                                startActivity(new Intent(SignupAsPatientActivity.this, BasicPatientActivity.class));
                                finish();
                            }
                            else{
                                String e ="Failed to create user:"+task.getException().getMessage();
                                Toast.makeText(SignupAsPatientActivity.this, e , Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                        }
                    });
                }
            }
        });
    }
    public void adduser() {

        radioGenButton = (RadioButton) findViewById(radioGenGroup.getCheckedRadioButtonId());
        String Name = nameEt.getText().toString();
        String Email = emailEt.getText().toString().trim();
        String Gender = radioGenButton.getText().toString();
        String PhNo = phnoEt.getText().toString();
        String dob=showDOB.getText().toString();
        ID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        /*PatientUser patientUser = new PatientUser(Name,Email,Gender,PhNo,dob,ID);
        dbr.child(ID).setValue(patientUser);*/
        dbr = dbr.child(ID);
        dbr.child("Name").setValue(Name);
        dbr.child("Email").setValue(Email);
        dbr.child("Gender").setValue(Gender);
        dbr.child("PhNo").setValue(PhNo);
        dbr.child("DOB").setValue(dob);
        dbr.child("ID").setValue(ID);
        upload(resultUri);
    }

    void upload(Uri uri){

        StorageReference storageReference= FirebaseStorage.getInstance().getReference().child("PatientImages");
        final StorageReference ref = storageReference.child("ProfilePic").child(Objects.requireNonNull(uri.getLastPathSegment()));
        ref.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        dbr.child("ProfilePhoto").setValue(String.valueOf(uri));
                    }
                });
            }
        });
    }
    private void showProgDialog() {
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

    }

}
