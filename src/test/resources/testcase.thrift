namespace java com.sentiance.thrift

struct LocationFix {
    1: required double longitude;
    2: required double latitude;
    3: optional i64 timestamp;
}
