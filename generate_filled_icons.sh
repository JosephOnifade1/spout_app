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
      android:strokeWidth="2.5"
      android:strokeLineCap="round"
      android:strokeLineJoin="round"
      android:fillColor="#00000000"
      android:pathData="$pathdata"/>
</vector>
XML
}
generate_icon "icon_growth_sprout_filled" "M12,22V12 M12,12C12,6.5 17,4 20,5C19,10 16,12 12,12z M12,14C12,9 8,7 5,8C6,12 9,14 12,14z"
generate_icon "icon_growth_seed_filled" "M3,18 L21,18 M5,14 L19,14 M7,10 L17,10 M12,10 V6 C12,3 16,3 17,5 C16,8 14,8 12,6 M12,8 C12,5 8,5 7,7 C8,10 10,10 12,8"
generate_icon "icon_growth_bloom_filled" "M12,12 m-4,0 a4,4 0 1,1 8,0 a4,4 0 1,1 -8,0 M12,8 C12,2 17,2 17,8 C17,11 14,12 12,12 M12,8 C12,2 7,2 7,8 C7,11 10,12 12,12 M12,16 C12,22 17,22 17,16 C17,13 14,12 12,12 M12,16 C12,22 7,22 7,16 C7,13 10,12 12,12 M16,12 C22,12 22,7 16,7 C13,7 12,10 12,12 M16,12 C22,12 22,17 16,17 C13,17 12,14 12,12 M8,12 C2,12 2,7 8,7 C11,7 12,10 12,12 M8,12 C2,12 2,17 8,17 C11,17 12,14 12,12"
generate_icon "icon_nav_you_filled" "M16,13V18A2,2 0,0 1,14 20H5A2,2 0,0 1,3 18V13A2,2 0,0 1,5 11H14A2,2 0,0 1,16 13zM14,11V7C14,5 12,4 9.5,4C7,4 5,5 5,7V11M16,15H20C21,15 22,14 22,13C22,12 21,10 19,8L18,7"
