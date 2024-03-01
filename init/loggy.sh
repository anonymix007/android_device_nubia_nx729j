#!/system/bin/sh
# loggy.sh.

_date=`date +%F_%H-%M-%S`
logcat -b all -v time -f /metadata/logcat_${_date}.txt &
dmesg -w > /metadata/kmsg_${_date}.txt
