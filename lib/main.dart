import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
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
  List<IntruderData> intruders = [];

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    _initializePermissions();
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.resumed) {
      _loadIntruders();
    }
  }

  Future<void> _initializePermissions() async {
    await [
      Permission.camera,
      Permission.locationWhenInUse,
      Permission.ignoreBatteryOptimizations,
      Permission.photos, // Optional but recommended for Android 13+
      Permission.storage, // Added storage permission
    ].request();

    try {
      await platform.invokeMethod('activateDeviceAdmin');
    } on PlatformException catch (e) {
      debugPrint("Erreur d'activation de Device Admin: ${e.message}");
    }

    _loadIntruders();
  }

  Future<void> _loadIntruders() async {
    final data = await IntruderDataLoader.loadIntruders();
    if (mounted) {
      setState(() => intruders = data);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Intrusions détectées'),
      ),
      body: intruders.isEmpty
          ? const Center(child: Text("Aucune intrusion détectée."))
          : ListView.builder(
              itemCount: intruders.length,
              itemBuilder: (context, index) {
                return PhotoCard(data: intruders[index]);
              },
            ),
    );
  }
}