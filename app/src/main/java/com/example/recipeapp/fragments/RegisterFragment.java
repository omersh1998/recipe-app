package com.example.recipeapp.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.recipeapp.R;
import com.example.recipeapp.activities.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RegisterFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RegisterFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    public RegisterFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RegisterFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RegisterFragment newInstance(String param1, String param2) {
        RegisterFragment fragment = new RegisterFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.register, container, false);

        Button newbutton = view.findViewById(R.id.register);
        TextView buttonToLogin = view.findViewById(R.id.goToLogin);

        newbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText email = (EditText) view.findViewById(R.id.email);
                EditText password = (EditText) view.findViewById(R.id.password);
                String emailText = email.getText().toString();
                String passwordText = password.getText().toString();

                registerUser(emailText, passwordText, view);
            }
        });

        buttonToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(view).navigate(R.id.action_RegisterFragment_to_LoginFragment);
            }
        });

        return view;
    }

    public void registerUser (String email,String password, View view) {
        MainActivity mainActivity = (MainActivity) getActivity();
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(mainActivity, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(getActivity(),"Registered!", Toast.LENGTH_LONG).show();

                        user = mAuth.getCurrentUser();
                        MainActivity mainActivity = (MainActivity) getActivity();

                        if (mainActivity != null && user != null) {
                            mainActivity.addData(user.getUid());
                        }

                        Navigation.findNavController(view).navigate(R.id.action_RegisterFragment_to_LoginFragment);
                    } else {
                        // If sign in fails, display a message to the user.
                        Exception e = task.getException();
                        Toast.makeText(getActivity(),e.getMessage(), Toast.LENGTH_LONG).show();
                        // Toast.makeText(getActivity(),"Email address already exists", Toast.LENGTH_LONG).show();
                    }
                }
            });
    }
}