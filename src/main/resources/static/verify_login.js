function verifyLogin() {
    let loginValidation = document.getElementById('login_validation');
    if (loginValidation) {
        loginValidation.remove();
    }
    let loginFormGroup = document.getElementById('login-form-group');
    let loginElement = document.getElementById('login');
    loginElement.className = 'form-control';
    let login = loginElement.value;
    const regex = /^[a-zA-Z0-9]{4,9}$/;
    if (login.match(regex)) {
        let loading = document.createElement('small');
        loading.className = 'form-text text-muted';
        loading.id = 'login_validation';
        loading.textContent = 'Checking login...';
        loginFormGroup.appendChild(loading);
        let request = new XMLHttpRequest();
        request.onreadystatechange = function () {
            if (request.readyState === 4) {
                loading.remove();
                if (request.status === 204) {
                    loginElement.className = 'form-control is-valid';
                    let success = document.createElement('div');
                    success.className = 'valid-feedback';
                    success.id = 'login_validation';
                    success.textContent = 'Looks good!';
                    loginFormGroup.appendChild(success);
                } else {
                    loginElement.className = 'form-control is-invalid';
                    let error = document.createElement('div');
                    error.className = 'invalid-feedback';
                    error.id = 'login_validation';
                    if (request.status === 200) {
                        error.textContent = 'User with this login already exists.';
                    } else {
                        error.textContent = 'An unexpected error occurred. Please reload the page.';
                    }
                    loginFormGroup.appendChild(error);
                }
            }
        }
        request.open('get', '/api/user/' + login, true);
        request.send();
    } else {
        loginElement.className = 'form-control is-invalid';
        let error = document.createElement('div');
        error.className = 'invalid-feedback';
        error.id = 'login_validation';
        error.textContent = 'Login must contain 4-9 characters: a-z, A-Z, or 0-9.';
        loginFormGroup.appendChild(error);
    }
}