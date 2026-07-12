#!/bin/bash
function generate_icon() {
  local name=$1
  local pathdata=$2
  cat << XML > /app/applet/app/src/main/res/drawable/${name}.xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
  <path
      android:strokeColor="#FF000000"
      android:strokeWidth="1.5"
      android:strokeLineCap="round"
      android:strokeLineJoin="round"
      android:fillColor="#00000000"
      android:pathData="$pathdata"/>
</vector>
XML
}

generate_icon "icon_habit_water" "M7,11 H17 L15.5,20 H8.5 Z M12,11 V5 M12,5 C12,1 17,0 18,3 C16,7 13,7 12,5 M12,7 C12,3 7,2 6,5 C8,9 11,9 12,7"
generate_icon "icon_habit_read" "M12,21 V7 M12,7 C9,5 5,6 3,7 V19 C5,18 9,17 12,19 M12,7 C15,5 19,6 21,7 V19 C19,18 15,17 12,19"
generate_icon "icon_habit_meditate" "M12,20 C12,20 6,17 6,11 C6,7 12,3 12,3 C12,3 18,7 18,11 C18,17 12,20 12,20 Z M12,20 C12,20 4,17 2,12 C2,9 5,7 5,7 C5,7 8,9 11,14 M12,20 C12,20 20,17 22,12 C22,9 19,7 19,7 C19,7 16,9 13,14"
generate_icon "icon_growth_bud" "M12,21 C12,21 5,15 5,9 C5,5 8.5,3 12,3 C15.5,3 19,5 19,9 C19,15 12,21 12,21 Z M12,21 V9"
generate_icon "icon_growth_seed" "M3,18 L21,18 M5,14 L19,14 M7,10 L17,10 M12,10 V6 C12,3 16,3 17,5 C16,8 14,8 12,6 M12,8 C12,5 8,5 7,7 C8,10 10,10 12,8"
generate_icon "icon_nav_settings" "M17,15 L17,9 C17,7 15,6 12,6 L7,6 C5,6 5,8 5,15 Z M9,6 C9,3 11,2 13,2 C15,2 17,4 17,6 M5,11 L2,7 M1.5,8.5 L4.5,5.5"
generate_icon "icon_nav_add" "M12,5 V19 M5,12 H19"
generate_icon "icon_growth_bloom" "M12,12 m-4,0 a4,4 0 1,1 8,0 a4,4 0 1,1 -8,0 M12,8 C12,2 17,2 17,8 C17,11 14,12 12,12 M12,8 C12,2 7,2 7,8 C7,11 10,12 12,12 M12,16 C12,22 17,22 17,16 C17,13 14,12 12,12 M12,16 C12,22 7,22 7,16 C7,13 10,12 12,12 M16,12 C22,12 22,7 16,7 C13,7 12,10 12,12 M16,12 C22,12 22,17 16,17 C13,17 12,14 12,12 M8,12 C2,12 2,7 8,7 C11,7 12,10 12,12 M8,12 C2,12 2,17 8,17 C11,17 12,14 12,12"
generate_icon "icon_onboarding_streak" "M12,12 m-4,0 a4,4 0 1,1 8,0 a4,4 0 1,1 -8,0 M12,8 C12,2 17,2 17,8 C17,11 14,12 12,12 M12,8 C12,2 7,2 7,8 C7,11 10,12 12,12 M12,16 C12,22 17,22 17,16 C17,13 14,12 12,12 M12,16 C12,22 7,22 7,16 C7,13 10,12 12,12 M16,12 C22,12 22,7 16,7 C13,7 12,10 12,12 M16,12 C22,12 22,17 16,17 C13,17 12,14 12,12 M8,12 C2,12 2,7 8,7 C11,7 12,10 12,12 M8,12 C2,12 2,17 8,17 C11,17 12,14 12,12"
generate_icon "icon_nav_today" "M12,22V12 M12,12C12,6.5 17,4 20,5C19,10 16,12 12,12z M12,14C12,9 8,7 5,8C6,12 9,14 12,14z"
generate_icon "icon_growth_sprout" "M12,22V12 M12,12C12,6.5 17,4 20,5C19,10 16,12 12,12z M12,14C12,9 8,7 5,8C6,12 9,14 12,14z"
