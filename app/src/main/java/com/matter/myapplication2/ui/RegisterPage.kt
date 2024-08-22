package com.matter.myapplication2.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.matter.myapplication2.R
import com.matter.myapplication2.navigateSingleTopTo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterPage(modifier: Modifier = Modifier, navController: NavHostController) {

    var usernameInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }
    var firstNameInput by remember { mutableStateOf("") }
    var lastNameInput by remember { mutableStateOf("") }
    var mailInput by remember { mutableStateOf("") }
    var registerClick by remember { mutableStateOf(false) }


    if (registerClick)
    {
         navController.navigateSingleTopTo("login")
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF79bbff),
                        Color(0xFF4a90e2),
                        Color(0xFF0077CC)
                    ),
                    startY = 0.0f,
                    endY = 1000.0f
                )
            ),
        verticalArrangement = Arrangement.Center, // 内容垂直居中
        horizontalAlignment = Alignment.CenterHorizontally // 内容水平居中
    ) {

        Text(
            text = stringResource(id = R.string.app_title),
            fontSize = 30.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(bottom = 20.dp)
                .shadow(4.dp, shape = RoundedCornerShape(8.dp))
                .background(color = Color(0XFF3A9EFD), shape = RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Box(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .width(IntrinsicSize.Min)
                .shadow(5.dp, RoundedCornerShape(16.dp))
                .clip(shape = RoundedCornerShape(16.dp))
                .background(color = MaterialTheme.colorScheme.surface)
                .padding(16.dp), // 添加内部填充
            contentAlignment = Alignment.Center // 将内容居中
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "注册",
                    fontSize = MaterialTheme.typography.titleLarge.fontSize,
                    modifier = Modifier.padding(top = 10.dp)
                )

                TextField(
                    value = usernameInput, onValueChange = { usernameInput = it },
                    label = {
                        Text(text = stringResource(id = R.string.username))
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Next
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.textFieldColors(
                        focusedIndicatorColor = Color.Transparent, // 去掉底部的横线
                        unfocusedIndicatorColor = Color.Transparent // 去掉底部的横线
                    ),
                    )

                TextField(
                    value = passwordInput, onValueChange = { passwordInput = it },
                    label = {
                        Text(text = stringResource(id = R.string.password))
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.textFieldColors(
                        focusedIndicatorColor = Color.Transparent, // 去掉底部的横线
                        unfocusedIndicatorColor = Color.Transparent // 去掉底部的横线
                    ),
                )

                TextField(value = passwordConfirm, onValueChange = { passwordConfirm = it } ,
                    label = {
                        Text(text = stringResource(id = R.string.password_confirm))
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.textFieldColors(
                        focusedIndicatorColor = Color.Transparent, // 去掉底部的横线
                        unfocusedIndicatorColor = Color.Transparent // 去掉底部的横线
                    ),
                )

                TextField(value = firstNameInput, onValueChange = { firstNameInput = it } ,
                    label = {
                        Text(text = "姓")
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.textFieldColors(
                        focusedIndicatorColor = Color.Transparent, // 去掉底部的横线
                        unfocusedIndicatorColor = Color.Transparent // 去掉底部的横线
                    ),
                )

                TextField(value = lastNameInput, onValueChange = { lastNameInput = it } ,
                    label = {
                        Text(text = "名")
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.textFieldColors(
                        focusedIndicatorColor = Color.Transparent, // 去掉底部的横线
                        unfocusedIndicatorColor = Color.Transparent // 去掉底部的横线
                    ),
                )

                TextField(value = mailInput, onValueChange = { mailInput = it } ,
                    label = {
                        Text(text = "邮箱")
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.textFieldColors(
                        focusedIndicatorColor = Color.Transparent, // 去掉底部的横线
                        unfocusedIndicatorColor = Color.Transparent // 去掉底部的横线
                    ),
                )


                Button(onClick = {
                    registerClick = !registerClick
                }) {
                    Text(
                        text = "注册",
                        fontSize = 20.sp
                    )
                }



            }


        }

    }
}