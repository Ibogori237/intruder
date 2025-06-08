import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_fingerprint_reconization/fingerprint_reconization.dart';
import 'package:permission_handler/permission_handler.dart';

import 'services/intruder_data.dart';
import 'widgets/photo_card.dart';
void main() {
  runApp(const IntruderApp());
}

class IntruderApp extends StatelessWidget {
  const IntruderApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Intruder',
      theme: ThemeData.dark(),
      home: const IntruderHome(),
      debugShowCheckedModeBanner: false,
    );
  }
}

class IntruderHome extends StatefulWidget {
  const IntruderHome({super.key});

  @override
  State<IntruderHome> createState() => _IntruderHomeState();
}

class _IntruderHomeState extends State<IntruderHome> with WidgetsBindingObserver {
  static const platform = MethodChannel('com.example.intruder/channel');
  final _fingerprintPlugin = FingerprintReconization();
  List<IntruderData> intruders = [];
  Map<String, String> photoLocations = {}; // Map to store photo paths and their locations

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    debugPrint("Initializing permissions...");
    _initializePermissions();
    Future.microtask(() => authenticateWithFingerprint());
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    debugPrint("App lifecycle state changed: $state");
    if (state == AppLifecycleState.resumed) {
      debugPrint("App resumed, loading intruders...");
      _loadIntruders();
    }
  }

  Future<void> _initializePermissions() async {
    debugPrint("Requesting permissions...");
    final statuses = await [
      Permission.camera,
      Permission.locationWhenInUse,
      Permission.ignoreBatteryOptimizations,
    ].request();

    debugPrint("Permission statuses: $statuses");

    try {
      debugPrint("Activating device admin...");
      await platform.invokeMethod('activateDeviceAdmin');
    } on PlatformException catch (e) {
      debugPrint("Erreur d'activation de Device Admin: ${e.message}");
    }

    debugPrint("Loading intruders after permissions...");
    _loadIntruders();
  }

  Future<void> _loadIntruders() async {
    try {
      debugPrint("Loading intruders...");
      final data = await IntruderDataLoader.loadIntruders();
      debugPrint("Loaded intruders: ${data.length}");
      if (mounted) {
        setState(() => intruders = data);
      }
    } catch (e) {
      debugPrint("Erreur lors du chargement des intrusions: $e");
    }
  }

  Future<bool> authenticateWithFingerprint() async {
    try {
      debugPrint("Authenticating with fingerprint...");
      final result = await _fingerprintPlugin.authenticate();
      debugPrint("Fingerprint authentication result: $result");
      if (result == 'AUTH_SUCCESS') {
        debugPrint("Empreinte reconnue !");
        await _loadIntruders();
        return true;
      } else {
        debugPrint("Échec d'authentification: $result");
        debugPrint("Taking photo from native...");
        await platform.invokeMethod('takePhotoFromNative');
        await _loadIntruders();
        return false;
      }
    } catch (e) {
      debugPrint("Erreur empreinte: $e");
      return false;
    }
  }

  Future<void> capturePhotoWithLocation() async {
  try {
    debugPrint("Capturing photo with location...");
    await platform.invokeMethod('takePhotoFromNative');
    final locationLink = await platform.invokeMethod<String>('getLocation');
    debugPrint("Location obtained: $locationLink");

    // Associate the location with the photo
    final photoPath = "/path/to/captured/photo"; // Replace with actual photo path
    photoLocations[photoPath] = locationLink ?? "Location unavailable";
    debugPrint("Photo location stored: $photoPath -> $locationLink");

    await _loadIntruders();
  } catch (e) {
    debugPrint("Erreur lors de la capture de la photo: $e");
  }
}

@override
Widget build(BuildContext context) {
  debugPrint("Building UI...");
  return Scaffold(
    appBar: AppBar(
      title: const Text('Intrusions détectées'),
    ),
    body: intruders.isEmpty
        ? Center(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                const Text("Aucune intrusion détectée."),
                const SizedBox(height: 20),
                ElevatedButton(
                  onPressed: authenticateWithFingerprint,
                  child: const Text("S'authentifier par empreinte"),
                ),
              ],
            ),
          )
        : GridView.builder(
            padding: const EdgeInsets.all(10),
            gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
              crossAxisCount: 2, // Number of columns
              crossAxisSpacing: 10, // Space between columns
              mainAxisSpacing: 10, // Space between rows
              childAspectRatio: 1, // Aspect ratio of each grid item
            ),
            itemCount: intruders.length,
            itemBuilder: (context, index) {
              debugPrint("Displaying intruder data at index $index...");
              final photoPath = intruders[index].image.path;
              final locationLink = photoLocations[photoPath] ?? "Localisation inconnue";
              return PhotoCard(
                data: intruders[index],
                authenticateWithFingerprint: authenticateWithFingerprint,
                locationLink: locationLink, // Pass location link
              );
            },
          ),
  );
}
}