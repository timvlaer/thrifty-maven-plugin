namespace java com.sentiance.thrift

const byte TEST1 = 1;
const byte TEST2 = 2;

union TestUnion {
    1: string value1;
    2: string value2;
    3: string value3;
}

struct TestStruct {
    1: string value1;
    2: string value2;
    3: string value3;
}
