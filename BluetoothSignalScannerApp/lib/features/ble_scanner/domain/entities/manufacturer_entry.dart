class ManufacturerEntry {
  final int companyId;
  final String companyName;
  final String payloadHex;

  const ManufacturerEntry({
    required this.companyId,
    required this.companyName,
    required this.payloadHex,
  });

  String get formattedHeader => '$companyName (0x${companyId.toRadixString(hexRadix).toUpperCase()})';

  static const int hexRadix = 16;
}

