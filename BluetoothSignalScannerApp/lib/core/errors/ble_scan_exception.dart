class BleScanException implements Exception {
  final String code;
  final String message;

  const BleScanException({
    required this.code,
    required this.message,
  });

  static const String invalidDurationCode = 'INVALID_DURATION';
  static const String unsupportedPlatformCode = 'UNSUPPORTED_PLATFORM';
  static const String platformChannelErrorCode = 'PLATFORM_CHANNEL_ERROR';
  static const String payloadParseErrorCode = 'PAYLOAD_PARSE_ERROR';

  @override
  String toString() => '[$code] $message';
}

