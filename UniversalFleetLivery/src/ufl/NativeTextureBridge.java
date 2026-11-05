package ufl;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.graphics.SpriteAPI;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/** One cached public binding for the verified Windows/Linux RC8 native image registrars. */
public final class NativeTextureBridge {
  private static MethodHandle registrar;
  private static String binding = "not initialised";

  private static synchronized MethodHandle resolve() throws IOException {
    if (registrar != null) return registrar;
    try {
      Class<?> manager = Class.forName("com.fs.graphics.oOoO");
      MethodType signature = MethodType.methodType(void.class, String.class, BufferedImage.class);
      // Official Linux RC8 uses o00000; official Windows RC8 uses the JVM name super.
      // Both are public static (String, BufferedImage) -> void. No private lookup or reflection.
      for (String name : new String[] {"o00000", "super"}) {
        try {
          registrar = MethodHandles.publicLookup().findStatic(manager, name, signature);
          binding = manager.getName() + "." + name;
          Global.getLogger(NativeTextureBridge.class)
              .info("UFL: in-memory texture registrar " + binding);
          return registrar;
        } catch (NoSuchMethodException e) {
          // Try the other independently verified RC8 distribution name.
        }
      }
      throw new IOException(
          "No supported RC8 native image registrar; Windows and Linux signatures both missing");
    } catch (ClassNotFoundException | IllegalAccessException | LinkageError e) {
      throw failure("resolve registrar", e);
    }
  }

  public static SpriteAPI register(String id, BufferedImage image) throws IOException {
    try {
      resolve().invokeExact(id, image);
      SpriteAPI sprite = Global.getSettings().getSprite(id);
      if (sprite == null || sprite.getTextureId() <= 0)
        throw new IOException("Native image registration returned no usable texture: " + binding);
      return sprite;
    } catch (Throwable e) {
      if (e instanceof VirtualMachineError) throw (VirtualMachineError) e;
      if (e instanceof ThreadDeath) throw (ThreadDeath) e;
      if (e instanceof IOException) throw (IOException) e;
      throw failure("register image via " + binding, e);
    }
  }

  private static IOException failure(String operation, Throwable cause) {
    return new IOException(
        "Cannot " + operation + ": " + cause.getClass().getSimpleName() + ": " + cause.getMessage(),
        cause);
  }

  public static String status() {
    return binding;
  }

  private NativeTextureBridge() {}
}
