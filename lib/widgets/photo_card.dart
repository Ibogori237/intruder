import 'dart:io';
import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import '../services/intruder_data.dart';

class PhotoCard extends StatelessWidget {
  final IntruderData data;
  final Future<bool> Function() authenticateWithFingerprint;
  final String locationLink;

  const PhotoCard({
    super.key,
    required this.data,
    required this.authenticateWithFingerprint,
    required this.locationLink,
  });

  @override
  Widget build(BuildContext context) {
    final formattedTime = DateFormat('dd/MM/yyyy HH:mm:ss').format(data.timestamp);

    return GestureDetector(
      onTap: () async {
        final isAuthenticated = await authenticateWithFingerprint();
        if (isAuthenticated) {
          showDialog(
            context: context,
            builder: (context) => AlertDialog(
              title: const Text('Détails de l\'intrusion'),
              content: Column(
                mainAxisSize: MainAxisSize.min,
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text('Date et heure : $formattedTime'),
                  const SizedBox(height: 8),
                  Text('Email envoyé à : ibrahimabakargori235@gmail.com'),
                  const SizedBox(height: 8),
                  Text('Localisation : $locationLink'),
                ],
              ),
              actions: [
                TextButton(
                  onPressed: () => Navigator.of(context).pop(),
                  child: const Text('Fermer'),
                ),
              ],
            ),
          );
        } else {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(content: Text('Authentification échouée')),
          );
        }
      },
      child: Card(
        margin: const EdgeInsets.all(5),
        elevation: 5,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
        child: Column(
          children: [
            Expanded(
              child: ClipRRect(
                borderRadius: const BorderRadius.vertical(top: Radius.circular(16)),
                child: Image.file(
                  data.image,
                  fit: BoxFit.cover,
                  width: double.infinity,
                ),
              ),
            ),
            Padding(
              padding: const EdgeInsets.all(8.0),
              child: Text(
                'Tentative détectée le $formattedTime',
                style: const TextStyle(fontSize: 12),
                textAlign: TextAlign.center,
              ),
            ),
          ],
        ),
      ),
    );
  }
}