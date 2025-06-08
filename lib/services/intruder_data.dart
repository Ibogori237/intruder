import 'dart:io';
import 'package:flutter/material.dart';
import 'package:path_provider/path_provider.dart';

class IntruderData {
  final File image;
  final DateTime timestamp;

  IntruderData({required this.image, required this.timestamp});
}

class IntruderDataLoader {
  static Future<List<IntruderData>> loadIntruders() async {
    final dir = await getExternalStorageDirectory(); // Use external files directory
    final intruderDir = Directory('${dir!.path}/Intruder');
    
    if (!await intruderDir.exists()) {
      debugPrint("Intruder directory does not exist.");
      return [];
    }

    final files = intruderDir
        .listSync()
        .whereType<File>()
        .where((f) => f.path.endsWith('.jpg'))
        .toList();

    debugPrint("Found ${files.length} intruder files.");

    // Sort files by last modified date (newest first)
    files.sort((a, b) => b.lastModifiedSync().compareTo(a.lastModifiedSync()));

    return files.map((file) {
      final fileName = file.path.split('/').last;
      debugPrint("Processing file: $fileName");

      // Use RegExp to extract the date string safely
      final match = RegExp(r'intruder_(\d{8}_\d{6})\.jpg').firstMatch(fileName);
      if (match != null) {
        final dateStr = match.group(1)!;
        try {
          // Manual parsing to avoid DateFormat issue
          final year = int.parse(dateStr.substring(0, 4));
          final month = int.parse(dateStr.substring(4, 6));
          final day = int.parse(dateStr.substring(6, 8));
          final hour = int.parse(dateStr.substring(9, 11));
          final minute = int.parse(dateStr.substring(11, 13));
          final second = int.parse(dateStr.substring(13, 15));

          final date = DateTime(year, month, day, hour, minute, second);

          return IntruderData(image: file, timestamp: date);
        } catch (e) {
          debugPrint("Error parsing date from filename: $dateStr. Error: $e");
          return null;
        }
      } else {
        debugPrint("Filename doesn't match expected pattern: $fileName");
        return null;
      }
    }).whereType<IntruderData>().toList();
  }
}
