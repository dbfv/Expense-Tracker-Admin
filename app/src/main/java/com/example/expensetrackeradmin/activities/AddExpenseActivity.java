package com.example.expensetrackeradmin.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensetrackeradmin.R;
import com.example.expensetrackeradmin.adapters.ExpenseImageAdapter;
import com.example.expensetrackeradmin.helpers.CloudinaryHelper;
import com.example.expensetrackeradmin.helpers.DatabaseHelper;
import com.example.expensetrackeradmin.helpers.LocationHelper;
import com.example.expensetrackeradmin.helpers.SyncTriggerHelper;
import com.example.expensetrackeradmin.models.Employee;
import com.example.expensetrackeradmin.models.Expense;
import com.example.expensetrackeradmin.models.ExpenseImage;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class AddExpenseActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 101;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 102;

    private TextInputEditText etExpenseDate;
    private TextInputEditText etAmount;
    private TextInputEditText etDescription;
    private TextInputEditText etLocation;
    private TextInputEditText etClaimantCode;
    private AutoCompleteTextView spCurrency;
    private AutoCompleteTextView spExpenseType;
    private AutoCompleteTextView spPaymentMethod;
    private AutoCompleteTextView spPaymentStatus;
    private TextView tvClaimantDisplay;
    private Button btnSaveExpense;
    private Button btnAddPhoto;
    private RecyclerView rvExpenseImagePreview;

    private DatabaseHelper dbHelper;
    private ExpenseImageAdapter expenseImageAdapter;

    private final List<Uri> selectedLocalImageUris = new ArrayList<>();
    private final List<String> existingUploadedImageUrls = new ArrayList<>();
    private final List<String> previewImageSources = new ArrayList<>();

    private String projectId;
    private String expenseId;
    private String selectedClaimantId;
    private Uri pendingCameraImageUri;
    private boolean isEditMode = false;

    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private final ActivityResultLauncher<Intent> pickImagesLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() != RESULT_OK || result.getData() == null) {
                    return;
                }

                Intent data = result.getData();
                if (data.getClipData() != null) {
                    for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                        Uri uri = data.getClipData().getItemAt(i).getUri();
                        if (uri != null) {
                            selectedLocalImageUris.add(uri);
                        }
                    }
                } else if (data.getData() != null) {
                    selectedLocalImageUris.add(data.getData());
                }

                refreshPreviewList();
            }
    );

    private final ActivityResultLauncher<Uri> takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            success -> {
                if (success && pendingCameraImageUri != null) {
                    selectedLocalImageUris.add(pendingCameraImageUri);
                    refreshPreviewList();
                }
            }
    );

    @Override
    protected void onResume() {
        super.onResume();
        SyncTriggerHelper.attemptSyncIfOnline(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        CloudinaryHelper.init(this);

        dbHelper = new DatabaseHelper(this);
        projectId = getIntent().getStringExtra("PROJECT_ID");
        expenseId = getIntent().getStringExtra("EXPENSE_ID");

        initViews();
        setupToolbar();
        setupDropdowns();
        setupClaimantCodeLookup();
        setupAmountFormatting();
        setupImagePickerUi();

        if (expenseId != null && !expenseId.isEmpty()) {
            isEditMode = true;
            loadExpenseData(expenseId);
        } else {
            etExpenseDate.setText(dateFormatter.format(new Date()));
            fetchCurrentLocation();
        }

        etExpenseDate.setOnClickListener(v -> showDatePicker(etExpenseDate));
        btnSaveExpense.setOnClickListener(v -> saveExpenseToDatabase());
    }

    private void setupImagePickerUi() {
        rvExpenseImagePreview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        expenseImageAdapter = new ExpenseImageAdapter(previewImageSources, true, position -> {
            if (position >= 0 && position < selectedLocalImageUris.size()) {
                selectedLocalImageUris.remove(position);
                refreshPreviewList();
            }
        });
        rvExpenseImagePreview.setAdapter(expenseImageAdapter);

        btnAddPhoto.setOnClickListener(v -> showImageSourceDialog());
        refreshPreviewList();
    }

    private void showImageSourceDialog() {
        CharSequence[] options = {"Camera", "Gallery"};
        new AlertDialog.Builder(this)
                .setTitle("Add photo")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openCamera();
                    } else {
                        openGallery();
                    }
                })
                .show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        pickImagesLauncher.launch(Intent.createChooser(intent, "Select images"));
    }

    private void openCamera() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
            return;
        }

        Uri imageUri = createTempImageUri();
        if (imageUri == null) {
            Toast.makeText(this, "Cannot open camera right now.", Toast.LENGTH_SHORT).show();
            return;
        }

        pendingCameraImageUri = imageUri;
        takePictureLauncher.launch(imageUri);
    }

    private Uri createTempImageUri() {
        try {
            File imageFile = File.createTempFile("expense_" + System.currentTimeMillis(), ".jpg", getCacheDir());
            return FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", imageFile);
        } catch (IOException e) {
            return null;
        }
    }

    private void refreshPreviewList() {
        previewImageSources.clear();
        for (Uri uri : selectedLocalImageUris) {
            previewImageSources.add(uri.toString());
        }
        expenseImageAdapter.updateData(previewImageSources);
        rvExpenseImagePreview.setVisibility(previewImageSources.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void setupAmountFormatting() {
        etAmount.addTextChangedListener(new TextWatcher() {
            private String current = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(current)) {
                    etAmount.removeTextChangedListener(this);
                    String cleanString = s.toString().replaceAll("[.,]", "");
                    if (!cleanString.isEmpty()) {
                        try {
                            long parsed = Long.parseLong(cleanString);
                            String formatted = String.format(Locale.getDefault(), "%,d", parsed).replace(",", ".");
                            current = formatted;
                            etAmount.setText(formatted);
                            etAmount.setSelection(formatted.length());
                        } catch (NumberFormatException ignored) {
                        }
                    } else {
                        current = "";
                        etAmount.setText("");
                    }
                    etAmount.addTextChangedListener(this);
                }
            }
        });
    }

    private void fetchCurrentLocation() {
        LocationHelper locationHelper = new LocationHelper(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );
            return;
        }

        locationHelper.getCurrentLocation(new LocationHelper.LocationCallback() {
            @Override
            public void onLocationFound(String address) {
                if (!isFinishing() && (etLocation.getText() == null || etLocation.getText().toString().trim().isEmpty())) {
                    etLocation.setText(address);
                }
                Toast.makeText(AddExpenseActivity.this, "Location detected!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(AddExpenseActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission denied.", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadExpenseData(String expenseId) {
        Expense expense = dbHelper.getExpenseById(expenseId);
        if (expense == null) {
            return;
        }

        projectId = expense.getProjectId();
        etExpenseDate.setText(expense.getDate());
        etAmount.setText(String.valueOf((long) expense.getAmount()));
        etDescription.setText(expense.getDescription());
        etLocation.setText(expense.getLocation());
        spCurrency.setText(expense.getCurrency(), false);
        spExpenseType.setText(expense.getType(), false);
        spPaymentMethod.setText(expense.getPaymentMethod(), false);
        spPaymentStatus.setText(expense.getStatus(), false);

        existingUploadedImageUrls.clear();
        if (expense.getImages() != null) {
            for (ExpenseImage image : expense.getImages()) {
                if (image.getImageUrl() != null && !image.getImageUrl().trim().isEmpty()) {
                    existingUploadedImageUrls.add(image.getImageUrl());
                }
            }
        }

        Employee claimant = dbHelper.getEmployeeById(expense.getClaimant());
        if (claimant != null) {
            selectedClaimantId = claimant.getId();
            etClaimantCode.setText(claimant.getCode());
            tvClaimantDisplay.setText(claimant.getName() + " - " + claimant.getCode());
            tvClaimantDisplay.setVisibility(View.VISIBLE);
        } else {
            selectedClaimantId = null;
            etClaimantCode.setText(expense.getClaimant());
            String claimantDisplay = expense.getClaimantDisplay();
            if (claimantDisplay != null && !claimantDisplay.trim().isEmpty()) {
                tvClaimantDisplay.setText(claimantDisplay);
                tvClaimantDisplay.setVisibility(View.VISIBLE);
            } else {
                tvClaimantDisplay.setVisibility(View.GONE);
            }
        }

        Toolbar toolbar = findViewById(R.id.toolbarAddExpense);
        toolbar.setTitle("Edit Expense");
        btnSaveExpense.setText("Update Expense");
    }

    private void initViews() {
        etExpenseDate = findViewById(R.id.etExpenseDate);
        etAmount = findViewById(R.id.etAmount);
        etDescription = findViewById(R.id.etDescription);
        etLocation = findViewById(R.id.etLocation);
        etClaimantCode = findViewById(R.id.etClaimantCode);
        tvClaimantDisplay = findViewById(R.id.tvClaimantDisplay);
        spCurrency = findViewById(R.id.spCurrency);
        spExpenseType = findViewById(R.id.spExpenseType);
        spPaymentMethod = findViewById(R.id.spPaymentMethod);
        spPaymentStatus = findViewById(R.id.spPaymentStatus);
        btnSaveExpense = findViewById(R.id.btnSaveExpense);
        btnAddPhoto = findViewById(R.id.btnAddPhoto);
        rvExpenseImagePreview = findViewById(R.id.rvExpenseImagePreview);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbarAddExpense);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void showDatePicker(TextInputEditText targetEditText) {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    targetEditText.setText(dateFormatter.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void setupDropdowns() {
        String[] currencies = {"USD", "EUR", "GBP", "JPY", "VND"};
        ArrayAdapter<String> currencyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, currencies);
        spCurrency.setAdapter(currencyAdapter);
        spCurrency.setText("USD", false);

        String[] expenseTypes = {"Travel", "Equipment", "Materials", "Services", "Software/Licenses", "Labour costs", "Utilities", "Miscellaneous"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, expenseTypes);
        spExpenseType.setAdapter(typeAdapter);

        String[] paymentMethods = {"Cash", "Credit Card", "Bank Transfer", "Cheque"};
        ArrayAdapter<String> methodAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, paymentMethods);
        spPaymentMethod.setAdapter(methodAdapter);

        String[] paymentStatuses = {"Paid", "Pending", "Reimbursed"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, paymentStatuses);
        spPaymentStatus.setAdapter(statusAdapter);
    }

    private void setupClaimantCodeLookup() {
        etClaimantCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                resolveClaimantByCode(s.toString().trim(), true);
            }
        });
    }

    private Employee resolveClaimantByCode(String claimantCode, boolean showNotFoundMessage) {
        if (claimantCode == null || claimantCode.trim().isEmpty()) {
            selectedClaimantId = null;
            tvClaimantDisplay.setVisibility(View.GONE);
            return null;
        }

        Employee employee = dbHelper.getEmployeeByCode(claimantCode.trim());
        if (employee == null) {
            selectedClaimantId = null;
            if (showNotFoundMessage) {
                tvClaimantDisplay.setText("No user found for this code.");
                tvClaimantDisplay.setVisibility(View.VISIBLE);
            } else {
                tvClaimantDisplay.setVisibility(View.GONE);
            }
            return null;
        }

        selectedClaimantId = employee.getId();
        tvClaimantDisplay.setText(employee.getName() + " - " + employee.getCode());
        tvClaimantDisplay.setVisibility(View.VISIBLE);
        return employee;
    }

    private void saveExpenseToDatabase() {
        String expenseDate = etExpenseDate.getText().toString().trim();
        String amountStr = etAmount.getText().toString().replace(".", "").trim();
        String currency = spCurrency.getText().toString().trim();
        String expenseType = spExpenseType.getText().toString().trim();
        String paymentMethod = spPaymentMethod.getText().toString().trim();
        String claimantCode = etClaimantCode.getText().toString().trim();
        String paymentStatus = spPaymentStatus.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String location = etLocation.getText().toString().trim();

        StringBuilder missingFields = new StringBuilder();
        if (expenseDate.isEmpty()) missingFields.append("• Date of Expense\n");
        if (amountStr.isEmpty()) missingFields.append("• Amount\n");
        if (currency.isEmpty()) missingFields.append("• Currency\n");
        if (expenseType.isEmpty()) missingFields.append("• Type of Expense\n");
        if (paymentMethod.isEmpty()) missingFields.append("• Payment Method\n");
        if (claimantCode.isEmpty()) missingFields.append("• Claimant Code\n");
        if (paymentStatus.isEmpty()) missingFields.append("• Payment Status\n");

        if (missingFields.length() > 0) {
            Toast.makeText(this, "Please fill required fields:\n" + missingFields, Toast.LENGTH_LONG).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount format!", Toast.LENGTH_SHORT).show();
            return;
        }

        Employee claimant = resolveClaimantByCode(claimantCode, false);
        if (claimant == null || selectedClaimantId == null) {
            Toast.makeText(this, "Invalid claimant code.", Toast.LENGTH_SHORT).show();
            return;
        }

        Expense expense = new Expense();
        expense.setProjectId(projectId);
        expense.setDate(expenseDate);
        expense.setAmount(amount);
        expense.setCurrency(currency);
        expense.setType(expenseType);
        expense.setPaymentMethod(paymentMethod);
        expense.setClaimant(selectedClaimantId);
        expense.setStatus(paymentStatus);
        expense.setDescription(description);
        expense.setLocation(location);

        if (isEditMode) {
            expense.setExpenseId(expenseId);
        } else {
            expense.setExpenseId(UUID.randomUUID().toString());
        }

        setSavingState(true);
        uploadSelectedImagesThenPersistExpense(expense);
    }

    private void uploadSelectedImagesThenPersistExpense(Expense expense) {
        if (selectedLocalImageUris.isEmpty()) {
            persistExpenseWithImageUrls(expense, new ArrayList<>(existingUploadedImageUrls));
            return;
        }

        uploadImageAtIndex(0, new ArrayList<>(), uploadedUrls -> {
            List<String> finalUrls = new ArrayList<>(existingUploadedImageUrls);
            finalUrls.addAll(uploadedUrls);
            persistExpenseWithImageUrls(expense, finalUrls);
        }, error -> {
            setSavingState(false);
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        });
    }

    private void uploadImageAtIndex(int index, List<String> uploadedUrls, UploadSuccessCallback successCallback, UploadErrorCallback errorCallback) {
        if (index >= selectedLocalImageUris.size()) {
            successCallback.onSuccess(uploadedUrls);
            return;
        }

        CloudinaryHelper.uploadImage(this,
                selectedLocalImageUris.get(index),
                new CloudinaryHelper.CloudinaryCallback() {
                    @Override
                    public void onSuccess(String imageUrl) {
                        uploadedUrls.add(imageUrl);
                        uploadImageAtIndex(index + 1, uploadedUrls, successCallback, errorCallback);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        errorCallback.onError("Image upload failed: " + errorMessage);
                    }
                }
        );
    }

    private void persistExpenseWithImageUrls(Expense expense, List<String> imageUrls) {
        boolean saved;

        if (isEditMode) {
            saved = dbHelper.updateExpense(expense);
            if (saved) {
                dbHelper.replaceExpenseImages(expense.getExpenseId(), imageUrls);
            }
        } else {
            saved = dbHelper.insertExpense(expense);
            if (saved) {
                for (String imageUrl : imageUrls) {
                    dbHelper.insertExpenseImage(expense.getExpenseId(), imageUrl);
                }
            }
        }

        setSavingState(false);

        if (saved) {
            SyncTriggerHelper.attemptSyncIfOnline(this);
            String message = isEditMode ? "Expense updated successfully!" : "Expense saved successfully!";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            String message = isEditMode ? "Error updating expense." : "Error saving expense.";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }

    private void setSavingState(boolean saving) {
        btnSaveExpense.setEnabled(!saving);
        btnAddPhoto.setEnabled(!saving);
    }

    private interface UploadSuccessCallback {
        void onSuccess(List<String> uploadedUrls);
    }

    private interface UploadErrorCallback {
        void onError(String errorMessage);
    }
}
