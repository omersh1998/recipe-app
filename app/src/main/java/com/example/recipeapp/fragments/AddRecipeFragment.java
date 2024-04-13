package com.example.recipeapp.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipeapp.R;
import com.example.recipeapp.activities.HomeActivity;
import com.example.recipeapp.adapters.IngredientsAdapter;
import com.example.recipeapp.models.Ingredient;
import com.example.recipeapp.models.Recipe;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddRecipeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddRecipeFragment extends Fragment {
    private static final String USER_NAME = "userName";
    private static final String USER_ID = "userId";
    private static final int CAMERA_PERM_CODE = 101;
    private static final int CAMERA_REQUEST_CODE = 102;
    private static final int PICK_REQUEST_CODE = 103;
    private String userName;
    private String userId;
    private DatabaseReference userRef;
    private DatabaseReference ingredientsRef;
    private DatabaseReference recipeRef;
    private HomeActivity homeActivity;
    private RecyclerView ingredientsRecyclerView;
    private IngredientsAdapter ingredientsAdapter;
    private Uri imageURI = null;
    private Button addImageButton;
    private Button addRecipeButton;
    private Button addImageByPhotoButton;
    private SearchView searchView;
    private ImageView recipeNewImageView;
    private ChipGroup type_add_new;
    private String type;
    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;
    private ActivityResultLauncher<Intent> take_photo;
    private EditText searchText;
    private Bitmap bitmap;
    private FirebaseAuth mAuth;

    public AddRecipeFragment(String userName, String userId) {
        this.userName = userName;
        this.userId = userId;
    }

    public static AddRecipeFragment newInstance(String userName, String userId) {
        AddRecipeFragment fragment = new AddRecipeFragment(userName, userId);
        Bundle args = new Bundle();
        args.putString(USER_NAME, userName);
        args.putString(USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userName = getArguments().getString(USER_NAME);
            userId = getArguments().getString(USER_ID);
        }
        mAuth = FirebaseAuth.getInstance();

        registerForImagePicker();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.add_recipe, container, false);

        FirebaseDatabase realTimeDb = FirebaseDatabase.getInstance();
        userRef = realTimeDb.getReference("users").child(userId);
        recipeRef = realTimeDb.getReference("recipes");
        ingredientsRef = realTimeDb.getReference("ingredients");

        homeActivity = (HomeActivity) getActivity();
        homeActivity.startLoading();

        homeActivity.ingredients = new ArrayList<>();

        ingredientsRecyclerView = createIngredientsRecycleView(rootView);

        recipeNewImageView = rootView.findViewById(R.id.recipeNewImageView);
        addImageByPhotoButton = rootView.findViewById(R.id.addImageByPhotoButton);
        addRecipeButton = rootView.findViewById(R.id.addRecipeButton);
        addImageButton = rootView.findViewById(R.id.addImageButton);
        searchText = rootView.findViewById(R.id.ingredientSearchText);
        Button addIngredientButton = rootView.findViewById(R.id.add_ingredient_button);

        type_add_new = rootView.findViewById(R.id.type_add_new);

        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // This method is called before the text is changed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // This method is called when the text is changed
            }

            @Override
            public void afterTextChanged(Editable s) {
                // This method is called after the text is changed
                String text = s.toString();
                ingredientsAdapter.filter(text);
//                ArrayList<Ingredient> filtered = new ArrayList<>();
//                for (Ingredient i: homeActivity.ingredients) {
//                    if(i.getName().toLowerCase().contains(text.toLowerCase())) filtered.add(i);
//                }
//
//                updateAdapter(filtered);
            }
        });

        type_add_new.setOnCheckedChangeListener((group, checkedId) -> {
            if(checkedId != -1) {
                Chip c = rootView.findViewById(checkedId);
                type = (String) c.getText();
            }
        });

        addImageByPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askCameraPermission();
            }
        });

        addImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createImagePicker();
            }
        });

        recipeNewImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createImagePicker();
            }
        });

        addIngredientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                homeActivity.replaceFragment(new AddIngredientFragment(userId));
            }
        });

        addRecipeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!homeActivity.chosenIngredientIds.isEmpty()) {
                    uploadImageAndRecipe(rootView);
                }
                else {
                    Toast.makeText(getActivity(),"Cannot add recipe without ingredients!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        getIngredientsData();

        return rootView;
    }

    private void askCameraPermission() {
        if(ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{android.Manifest.permission.CAMERA}, CAMERA_PERM_CODE);
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        take_photo.launch(camera);
    }

    private void updateAdapter(ArrayList<Ingredient> ingredientsToUse) {
        // Initialize or update the adapter
        if (ingredientsAdapter == null) {
            ingredientsAdapter = new IngredientsAdapter(getActivity(), ingredientsToUse, ingredientsRef, userRef, userId);
            ingredientsRecyclerView.setAdapter(ingredientsAdapter);
        } else {
            ingredientsAdapter.setIngredientList(ingredientsToUse);
        }
    }

    private void uploadImageAndRecipe (View rootView) {
        // Generate a new unique key
        String key = recipeRef.push().getKey();

        if (imageURI != null) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();

            // Create a reference to the location where you want to store the image in Firebase Storage
            StorageReference imagesRef = storageRef.child("images/" + key);
            // Upload the image to Firebase Storage
            UploadTask uploadTask = imagesRef.putFile(imageURI);

            // Register observers to listen for upload success or failure
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // Image uploaded successfully, now get the download URL
                    imagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            // Get the download URL of the uploaded image
                            String imageUrl = uri.toString();

                            addRecipe(rootView, imageUrl, key);

                            // Handle successful recipe upload
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Handle failed image upload
                    addRecipe(rootView, null, key);
                }
            });
        }
        else {
            addRecipe(rootView, null, key);
        }
    }

    private void addRecipe (View rootView, @Nullable String imageUrl, String key) {
        EditText recipeName = (EditText) rootView.findViewById(R.id.recipeNameEditText);
        String recipeNameText = recipeName.getText().toString();

        EditText description = (EditText) rootView.findViewById(R.id.descriptionEditText);
        String descriptionText = description.getText().toString();

        ArrayList<Ingredient> ingredientsToAdd = onSaveRecipeButtonClick();

        Recipe recipe = new Recipe(recipeNameText, ingredientsToAdd, descriptionText, imageUrl, userId);

        recipeRef.child(key).setValue(recipe)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Document successfully saved
                        Toast.makeText(getActivity(),"Recipe Added!", Toast.LENGTH_SHORT).show();
                        homeActivity.replaceFragment(new AccountFragment(userName, userId));
                        homeActivity.chosenIngredientIds.clear();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to save document
                        Toast.makeText(getActivity(),e.getMessage(), Toast.LENGTH_LONG).show();
                        homeActivity.chosenIngredientIds.clear();
                    }
                });
    }

    private ArrayList<Ingredient> onSaveRecipeButtonClick() {
        searchText.setText("");
        ArrayList<Ingredient> ingredientsToAdd = new ArrayList<>();
        for (int i = 0; i < homeActivity.chosenIngredientIds.size(); i++) {
            String ingredientId = homeActivity.chosenIngredientIds.get(i);
            int ingredientIndex = findIngredientIndexFromId(ingredientId);
            String amount = ingredientsAdapter.getEditTextText(ingredientIndex);
            String name = homeActivity.ingredients.get(ingredientIndex).getName();
            ingredientsToAdd.add(new Ingredient(name, amount, ingredientId));
        }

        return ingredientsToAdd;
    }

    private int findIngredientIndexFromId(String id) {
        for (int i = 0; i < homeActivity.ingredients.size(); i++) {
            if (homeActivity.ingredients.get(i).getId().equals(id)) {
                return i;
            }
        }

        return -1;
    }

    public RecyclerView createIngredientsRecycleView (View rootView) {
        RecyclerView recyclerView = rootView.findViewById(R.id.ingredientsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        return recyclerView;
    }

    public void getIngredientsData () {
        ingredientsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                homeActivity.ingredients.clear();

                for (DataSnapshot ingredientSnapshot : dataSnapshot.getChildren()) {
                    // Get the list of ingredients
                    String ingredientId = ingredientSnapshot.getKey();
                    String ingredientValue = (String) ingredientSnapshot.getValue();
                    homeActivity.ingredients.add(new Ingredient(ingredientValue, null, ingredientId));
                }

                homeActivity.stopLoading();

                updateAdapter(homeActivity.ingredients);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
                homeActivity.stopLoading();
            }
        });
    }

    private void createImagePicker () {
        // Launch the photo picker and let the user choose only images.
        pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    private void registerForImagePicker () {
        // Register for activity result
        pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            // Callback is invoked after the user selects a media item or closes the photo picker.
            imageURI = uri;
            if (uri != null) {
                recipeNewImageView.setImageURI(imageURI);
            }
        });

        take_photo = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode() == Activity.RESULT_OK) {
                    Bundle bundle = result.getData().getExtras();
                    bitmap = (Bitmap) bundle.get("data");
                    recipeNewImageView.setImageBitmap(bitmap);
                }
            }
        });
    }


    private void showImagePreview () {
        try {
            // Open an input stream from the image URI
            InputStream inputStream = requireContext().getContentResolver().openInputStream(imageURI);

            // Decode the input stream into a Bitmap
            Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);

            // Close the input stream
            inputStream.close();

            // Define the desired dimensions for the resized bitmap
            int desiredWidth = 100; // Adjust as needed
            int desiredHeight = 100; // Adjust as needed

            // Resize the bitmap
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, desiredWidth, desiredHeight, true);

            // Display the resized bitmap in an ImageView
            recipeNewImageView.setImageBitmap(resizedBitmap);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}