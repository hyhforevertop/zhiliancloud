
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.matter.myapplication2.R
import com.matter.myapplication2.navigateSingleTopTo
import com.matter.myapplication2.ui.theme.TopbarColor


@Composable
fun MainScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    paddingContent:@Composable (PaddingValues)->Unit,
    currentPage:String,

) {
     val homeIcon = if (currentPage=="home")
        R.drawable.home_selected
    else
        R.drawable.home_common

    val personIcon = if (currentPage=="profile")
        R.drawable.person_selected
    else
        R.drawable.person_common

    val coroutineScope = rememberCoroutineScope()
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopBar(ofWhich = navController.currentDestination?.route.toString(),navController)
        },
        containerColor = MaterialTheme.colorScheme.background, // 使用主题颜色
        content = {
                  paddingValues -> paddingContent(paddingValues)
        },
        bottomBar = {
            if (currentPage!="deviceDetail")
            BottomBar(
                modifier = modifier,
                homeIcon = homeIcon,
                personIcon = personIcon,
                onHomeIconClick = {
                        navController.navigateSingleTopTo("home")
                },
                onPersonIconClick = {
                        navController.navigateSingleTopTo("profile")
                }
            )
        },


    )
}

@Composable
fun BottomBar(
    modifier: Modifier = Modifier,
    @DrawableRes homeIcon: Int,
    @DrawableRes personIcon: Int,
    onHomeIconClick: () -> Unit,
    onPersonIconClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(color = MaterialTheme.colorScheme.surface)
    ) {
        IconButton(
            modifier = Modifier.weight(1f),
            onClick = onHomeIconClick
        ) {

            Icon(
                painter = painterResource(id = homeIcon),
                contentDescription = null,
            )

        }
        IconButton(
            modifier = Modifier.weight(1f),
            onClick = onPersonIconClick
        ) {


                Icon(
                    painter = painterResource(id = personIcon),
                    contentDescription = null
                )

           
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    ofWhich:String,
    navController: NavHostController
)
{
    val titleText= when(ofWhich)
    {
        "home"->stringResource(id = R.string.app_title)
        "profile"->stringResource(id = R.string.profile_info)
        "deviceDetail/{deviceId}"-> stringResource(R.string.device_details)
        else->""
    }
    TopAppBar(
        title = { Text(
            text = titleText
        ,
          color = Color.White
            )},
        navigationIcon = {  }, // 主页面没有返回按钮
        actions = {
            if (ofWhich=="home")
            IconButton(onClick = {
                navController.navigate("paring_selection")
            }) {
                Icon(painter = painterResource(id = R.drawable.add_circle),
                    contentDescription = null,
                    tint = Color.White
                    )
            }
        },
        colors =TopAppBarDefaults.topAppBarColors(
            containerColor = TopbarColor,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        )
        , modifier = Modifier
    )
}

