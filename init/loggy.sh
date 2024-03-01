#!/system/bin/sh
# loggy.sh.

echo "Hello from loggy" > /dev/kmsg

echo "42" > /metadata/loggy_test.txt

_date=`date +%F_%H-%M-%S`
logcat -b all -v time -f  /metadata/logcat_${_date}.txt &
dmesg -w > /metadata/kmsg_${_date}.txt
