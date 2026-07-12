import re

with open('/app/applet/app/src/main/java/com/example/ui/screens/TodayScreen.kt', 'r') as f:
    content = f.read()

# Add import for BottomNavBar
if 'import com.example.ui.components.BottomNavBar' not in content:
    content = content.replace('import com.example.ui.components.GrowthIcon', 'import com.example.ui.components.GrowthIcon\nimport com.example.ui.components.BottomNavBar')

# Replace the NavigationBar block
pattern = r"bottomBar\s*=\s*\{\s*NavigationBar\s*\([\s\S]*?\}\s*\)\s*\{\s*padding\s*->"

replacement = """bottomBar = {
            BottomNavBar(
                currentRoute = "today",
                onNavigate = { route -> 
                    if (route == "settings") onSettingsClick() 
                }
            )
        }
    ) { padding ->"""

content = re.sub(pattern, replacement, content)

with open('/app/applet/app/src/main/java/com/example/ui/screens/TodayScreen.kt', 'w') as f:
    f.write(content)
