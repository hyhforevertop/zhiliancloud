package com.matter.myapplication2.ui

import MainScreen
import TokenManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.matter.myapplication2.navigateSingleTopTo

@Composable
fun ProfilePage(modifier: Modifier = Modifier, navController: NavHostController) {

    MainScreen(
        modifier = modifier,
        navController = navController,
        paddingContent = { paddingValues ->
            Surface(
                modifier = Modifier.padding(paddingValues)
                    .padding(vertical = 10.dp, horizontal = 10.dp)
                    .shadow(2.dp, RoundedCornerShape(16.dp))
                    .clip(shape = RoundedCornerShape(16.dp))
                    ,
                color = Color.White
            ) {

            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.Start

            ) {

                Row(
                    modifier= Modifier
                        .height(100.dp)
                        .fillMaxWidth()

                        ,
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)

                ) {

                    Image(imageVector = Icons.Default.AccountCircle
                        , contentDescription = null,
                        modifier=Modifier.size(80.dp)
                    )

                    Text(text = TokenManager.getUsername().toString(),
                        fontSize = 20.sp)


                }


                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    onClick = {
                        TokenManager.clearToken()
                        navController.navigateSingleTopTo("login")

                    }) {
                    Text(text = "退出登录")
                }
            }

        }
        },
        currentPage = "profile"
    )

}

@Preview(showBackground = true)
@Composable
fun ProfilePagePreview() {
    ProfilePage(navController = NavHostController(LocalContext.current))
}