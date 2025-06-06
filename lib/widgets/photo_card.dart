import 'dart:io';
import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import '../services/intruder_data.dart';

class PhotoCard extends StatelessWidget {
  final IntruderData data;

  const PhotoCard({super.key, required this.data});

  @override
  Widget build(BuildContext context) {
    final formattedTime = DateFormat('dd/MM/yyyy HH:mm:ss').format(data.timestamp);
    return Card(
      margin: const EdgeInsets.all(10),
      elevation: 5,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      child: Column(
        children: [
          Image.file(data.image, fit: BoxFit.cover),
          const SizedBox(height: 8),
          Text(
            'Tentative détectée le $formattedTime',
            style: const TextStyle(fontSize: 14),
          ),
          const SizedBox(height: 10),
        ],
      ),
    );
  }
}
