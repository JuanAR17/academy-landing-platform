import { AbstractControl, ValidationErrors, ValidatorFn } from "@angular/forms";

export const passwordMatchValidator: ValidatorFn = (
  control: AbstractControl) : ValidationErrors | null => {
    const password = control.get('password')
    const confirmPassword = control.get('confirmPassword')

    if (!password || !confirmPassword || password.value === '' || confirmPassword.value === '') {
    return null;
  }

  if (password.value !== confirmPassword.value) {
    confirmPassword.setErrors({ mismatch: true }); 
    return { mismatch: true }; 
  } else {
    if (confirmPassword.hasError('mismatch')) {
        confirmPassword.setErrors(null);
    }
    return null; 
  }
};

export const passwordStrengthValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const value = control.value;

  if (!value) {
    return null; // Dejar que Validators.required maneje si el campo está vacío
  }

  const hasLetter = /[A-Za-z]+/.test(value);
  const hasNumber = /\d+/.test(value);

  const validationErrors: ValidationErrors = {};

  if (!hasLetter) {
    validationErrors['noLetter'] = true;
  }

  if (!hasNumber) {
    validationErrors['noNumber'] = true;
  }

  return Object.keys(validationErrors).length ? validationErrors : null;
};
  