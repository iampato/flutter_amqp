#import "FlutterAmqpPlugin.h"
#if __has_include(<flutter_amqp/flutter_amqp-Swift.h>)
#import <flutter_amqp/flutter_amqp-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "flutter_amqp-Swift.h"
#endif

@implementation FlutterAmqpPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftFlutterAmqpPlugin registerWithRegistrar:registrar];
}
@end
