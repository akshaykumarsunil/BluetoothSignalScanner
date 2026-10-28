enum BleProtocolType {
  eddystoneUid('EDDYSTONE_UID', 'Eddystone-UID'),
  eddystoneUrl('EDDYSTONE_URL', 'Eddystone-URL'),
  eddystoneTlm('EDDYSTONE_TLM', 'Eddystone-TLM'),
  altBeacon('ALT_BEACON', 'AltBeacon'),
  sig('SIG', 'Bluetooth SIG GAP');

  final String wireValue;
  final String displayName;

  const BleProtocolType(this.wireValue, this.displayName);

  static BleProtocolType fromWireValue(String? rawValue) {
    for (final protocol in BleProtocolType.values) {
      if (protocol.wireValue == rawValue) {
        return protocol;
      }
    }
    return BleProtocolType.sig;
  }
}

