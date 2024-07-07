package com.example.emergetechfinal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class Admin extends AppCompatActivity {
    TextView tvUsername;
    EditText email, password, name, currentName, newName, newPassword;
    Button registerBtn, goToLogin, viewBtn, updateBtn, deleteBtn;
    boolean valid = true;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    FirebaseUser user;
    CheckBox isAdminBox, isUserBox;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        user = fAuth.getCurrentUser();
        tvUsername = findViewById(R.id.username);
        name = findViewById(R.id.registerName);
        email = findViewById(R.id.registerEmail);
        password = findViewById(R.id.registerPassword);
        registerBtn = findViewById(R.id.registerBtn);
        goToLogin = findViewById(R.id.gotoLogin);

        isAdminBox = findViewById(R.id.isAdmin);
        isUserBox = findViewById(R.id.isUser);

        viewBtn = findViewById(R.id.viewBtn);
        updateBtn = findViewById(R.id.updateBtn);
        deleteBtn = findViewById(R.id.deleteBtn);

        currentName = findViewById(R.id.currentName);
        newName = findViewById(R.id.newName);
        newPassword = findViewById(R.id.newPassword);

        if (user != null) {
            DocumentReference df = fStore.collection("Users").document(user.getUid());
            df.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    tvUsername.setText("Welcome! " + documentSnapshot.getString("Name"));
                }
            });
        }

        isUserBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(compoundButton.isChecked()){
                    isAdminBox.setChecked(false);
                }
            }
        });

        isAdminBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(compoundButton.isChecked()){
                    isUserBox.setChecked(false);
                }
            }
        });


        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkField(email);
                checkField(password);
                checkField(name);
                if(!(isAdminBox.isChecked() || isUserBox.isChecked())){
                    Toast.makeText(Admin.this, "Select Account Type", Toast.LENGTH_SHORT).show();
                    return;
                }


                if (valid) {
                    if (isAdminBox.isChecked()) {
                        DocumentReference df = fStore.collection("Users").document(fAuth.getCurrentUser().getUid());
                        df.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.getString("isSuperAdmin") != null && documentSnapshot.getString("isSuperAdmin").equals("1")) {
                                    createAccount();
                                } else {
                                    Toast.makeText(Admin.this, "Only Super Admins can create Admin accounts", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        createAccount();
                    }
                }
            }
        });

        goToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(), Login.class));
                finish();
            }
        });
        // View Button
        viewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fStore.collection("Users").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        StringBuilder userData = new StringBuilder();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            userData.append("Name: ").append(document.getString("Name")).append("\n");
                            userData.append("Email: ").append(document.getString("UserEmail")).append("\n");
                            userData.append("Password: ").append(document.getString("Password")).append("\n\n");
                        }
                        // Display userData in a TextView or other UI component
                        Toast.makeText(Admin.this, userData.toString(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        // Update Button
        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldName = currentName.getText().toString();
                String updatedName = newName.getText().toString();
                String updatedPassword = newPassword.getText().toString();

                fStore.collection("Users").whereEqualTo("Name", oldName).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            DocumentReference userRef = document.getReference();
                            Map<String, Object> updatedInfo = new HashMap<>();
                            if (!updatedName.isEmpty()) {
                                updatedInfo.put("Name", updatedName);
                            }
                            if (!updatedPassword.isEmpty()) {
                                updatedInfo.put("Password", updatedPassword);
                            }
                            userRef.update(updatedInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(Admin.this, "User Updated", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });
            }
        });

        // Delete Button
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userNameToDelete = currentName.getText().toString();

                DocumentReference df = fStore.collection("Users").document(fAuth.getCurrentUser().getUid());
                df.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.getString("isSuperAdmin") != null && documentSnapshot.getString("isSuperAdmin").equals("1")) {
                            // Super Admin can delete both admins and users
                            deleteUser(userNameToDelete);
                        } else {
                            // Admin can only delete users
                            fStore.collection("Users").whereEqualTo("Name", userNameToDelete).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                        if (document.getString("isAdmin") == null || !document.getString("isAdmin").equals("1")) {
                                            deleteUser(userNameToDelete);
                                        } else {
                                            Toast.makeText(Admin.this, "SuperAdmins can only delete Admins", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            });
                        }
                    }
                });
            }
        });


    }
    private void deleteUser(String userNameToDelete) {
        fStore.collection("Users").whereEqualTo("Name", userNameToDelete).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    document.getReference().delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(Admin.this, "User Deleted", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }



    private void createAccount() {
        fAuth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                FirebaseUser user = fAuth.getCurrentUser();
                Toast.makeText(Admin.this, "Account Created", Toast.LENGTH_SHORT).show();
                DocumentReference df = fStore.collection("Users").document(user.getUid());
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("Name", name.getText().toString());
                userInfo.put("UserEmail", email.getText().toString());
                userInfo.put("Password", password.getText().toString());

                if (isAdminBox.isChecked()) {
                    userInfo.put("isAdmin", "1");
                }
                if (isUserBox.isChecked()) {
                    userInfo.put("isUser", "1");
                }

                df.set(userInfo);
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(Admin.this, "Failed to Create Account", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public boolean checkField(EditText textField){
        if(textField.getText().toString().isEmpty()){
            textField.setError("Error");
            valid = false;
        }else {
            valid = true;
        }

        return valid;
    }
}
