$(function () {

	// =========================
	// USER REGISTER VALIDATION
	// =========================

	var $userRegister = $("#userRegister");

	$userRegister.validate({

		rules: {
			name: {
				required: true,
				lettersonly: true
			},
			email: {
				required: true,
				space: true,
				email: true
			},
			mobileNumber: {
				required: true,
				space: true,
				numericOnly: true,
				minlength: 9,
				maxlength: 9
			},
			password: {
				required: true,
				space: true
			},
			confirmpassword: {
				required: true,
				space: true,
				equalTo: '#pass'
			},
			address: {
				required: true,
				all: true
			},
			district: {
				required: true,
				space: true
			},
			department: {
				required: true,
				space: true
			},
			pincode: {
				required: true,
				space: true,
				numericOnly: true
			},
			img: {
				required: true
			}
		},

		messages: {
			name: {
				required: 'requiere nombre',
				lettersonly: 'nombre inválido'
			},
			email: {
				required: 'El correo electrónico es obligatorio',
				space: 'espacio no permitido',
				email: 'correo no válido'
			},
			mobileNumber: {
				required: 'Número de móvil requerido',
				space: 'espacio no permitido',
				numericOnly: 'número inválido',
				minlength: 'mínimo 9 dígitos',
				maxlength: 'máximo 9 dígitos'
			},
			password: {
				required: 'La contraseña es obligatoria',
				space: 'espacio no permitido'
			},
			confirmpassword: {
				required: 'Confirme la contraseña',
				space: 'espacio no permitido',
				equalTo: 'Las contraseñas no coinciden'
			},
			address: {
				required: 'La dirección es obligatoria',
				all: 'inválido'
			},
			district: {
				required: 'El distrito es obligatorio',
				space: 'espacio no permitido'
			},
			department: {
				required: 'El departamento es obligatorio',
				space: 'espacio no permitido'
			},
			pincode: {
				required: 'Código postal requerido',
				space: 'espacio no permitido',
				numericOnly: 'código postal inválido'
			},
			img: {
				required: 'Imagen requerida'
			}
		}
	});


	// =========================
	// ORDER VALIDATION
	// =========================

	var $orders = $("#orders");

	$orders.validate({

		rules: {
			firstName: {
				required: true,
				lettersonly: true
			},
			lastName: {
				required: true,
				lettersonly: true
			},
			email: {
				required: true,
				space: true,
				email: true
			},
			mobileNo: {
				required: true,
				space: true,
				numericOnly: true,
				minlength: 9,
				maxlength: 9
			},
			address: {
				required: true,
				all: true
			},
			district: {
				required: true,
				all: true
			},
			department: {
				required: true,
				all: true
			},
			pincode: {
				required: true,
				space: true,
				numericOnly: true
			},
			paymentType: {
				required: true
			}
		},

		messages: {
			firstName: {
				required: 'requiere nombre',
				lettersonly: 'nombre inválido'
			},
			lastName: {
				required: 'requiere apellido',
				lettersonly: 'apellido inválido'
			},
			email: {
				required: 'Correo obligatorio',
				space: 'espacio no permitido',
				email: 'correo inválido'
			},
			mobileNo: {
				required: 'Número requerido',
				space: 'espacio no permitido',
				numericOnly: 'número inválido',
				minlength: 'mínimo 9 dígitos',
				maxlength: 'máximo 9 dígitos'
			},
			address: {
				required: 'La dirección es obligatoria',
				all: 'inválido'
			},
			district: {
				required: 'El distrito es obligatorio',
				all: 'inválido'
			},
			department: {
				required: 'El departamento es obligatorio',
				all: 'inválido'
			},
			pincode: {
				required: 'Código postal requerido',
				space: 'espacio no permitido',
				numericOnly: 'código postal inválido'
			},
			paymentType: {
				required: 'Seleccione el tipo de pago'
			}
		}
	});


	// =========================
	// RESET PASSWORD VALIDATION
	// =========================

	var $resetPassword = $("#resetPassword");

	$resetPassword.validate({

		rules: {
			password: {
				required: true,
				space: true
			},
			confirmPassword: {
				required: true,
				space: true,
				equalTo: '#pass'
			}
		},

		messages: {
			password: {
				required: 'La contraseña es obligatoria',
				space: 'espacio no permitido'
			},
			confirmPassword: {
				required: 'Confirme la contraseña',
				space: 'espacio no permitido',
				equalTo: 'Las contraseñas no coinciden'
			}
		}
	});

});


// =========================
// CUSTOM VALIDATORS
// =========================

jQuery.validator.addMethod('lettersonly', function (value) {
	return /^[^-\s][a-zA-Z_\s-]+$/.test(value);
});



jQuery.validator.addMethod('all', function (value) {
	return /^[^-\s][a-zA-Z0-9_,.\s-]+$/.test(value);
});

jQuery.validator.addMethod('numericOnly', function (value) {
	return /^[0-9]+$/.test(value);
});
