import 'dart:async';

import 'package:bloo_dot_evolutions/main.dart';
import 'package:flutter/material.dart';

class LoadScreenWidget extends StatefulWidget {
  const LoadScreenWidget({Key? key}) : super(key: key);

  @override
  State<StatefulWidget> createState() => LoadScreenState();
}

class LoadScreenState extends State<LoadScreenWidget> {
  late Timer timer;

  @override
  initState() {
    super.initState();
    timer = Timer(const Duration(seconds: 15), _onFinishedLoading);
  }

  @override
  Widget build(BuildContext context) {
    return Focus(
        child: GestureDetector(
            behavior: HitTestBehavior.translucent,
            onTap: () async => _onFinishedLoading(),
            child: Directionality(
                textDirection: TextDirection.ltr,
                child: Column(children: const [
                  Text("blooDot Evolutions",
                      textAlign: TextAlign.center,
                      style: TextStyle(fontFamily: "Courgette", fontSize: 53, color: Colors.blueAccent, shadows: [
                        Shadow(
                          offset: Offset(2.2, 2.2),
                          blurRadius: 13.0,
                          color: Color.fromARGB(255, 250, 250, 250),
                        )
                      ])),
                  SizedBox(height: 50),
                  Text("© 2019-2021 flamingin sarjakuvat oy",
                      textAlign: TextAlign.center, style: TextStyle(fontSize: 20, color: Colors.blueAccent))
                ], mainAxisAlignment: MainAxisAlignment.center, crossAxisAlignment: CrossAxisAlignment.center))));
  }

  void _onFinishedLoading() {
    timer.cancel();
    runApp(const BlooDotEvolutionsWidget());
  }
}
