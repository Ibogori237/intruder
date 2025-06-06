import 'dart:io';
import 'package:intl/intl.dart';

class IntruderData {
  final File image;
  final DateTime timestamp;

  IntruderData({required this.image, required this.timestamp});
}

class IntruderDataLoader {
  static Future<List<IntruderData>> loadIntruders() async {
    // Chemin où les photos sont stockées - adapte si besoin
    final dir = Directory('/storage/emulated/0/DCIM/Intruder');
    if (!await dir.exists()) return [];

    final files = dir
        .listSync()
        .whereType<File>()
        .where((f) => f.path.endsWith('.jpg'))
        .toList();

    // Tri par date décroissante (les plus récentes d'abord)
    files.sort((a, b) => b.lastModifiedSync().compareTo(a.lastModifiedSync()));

    return files.map((file) {
      // Extraction de la date depuis le nom de fichier (intruder_YYYYMMDD_HHmmss.jpg)
      final fileName = file.path.split('/').last;
      final dateStr = fileName.split('_').sublist(1).join('_').replaceAll('.jpg', '');
      final date = DateFormat('yyyyMMdd_HHmmss').parse(dateStr);
      return IntruderData(image: file, timestamp: date);
    }).toList();
  }
}
