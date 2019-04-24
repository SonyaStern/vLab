package vlab.server_java.manager;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import rlcp.echo.RlcpEchoRequest;
import rlcp.echo.RlcpEchoRequestBody;
import rlcp.generate.GeneratingResult;
import rlcp.generate.RlcpGenerateRequest;
import rlcp.generate.RlcpGenerateRequestBody;
import rlcp.generate.RlcpGenerateResponse;

public class ProcessManager {

  private static List<Process> serverList = new ArrayList<>();

  static Process starter() throws IOException {

    Process server = new ProcessBuilder(
        "java", "-cp",
        "\".\\out\\production\\vLab_prototype;.\\lib\\*\"",
        "vlab.server_java.Starter").start();

    new Thread(() -> {
      try {
        Reader errorReader = new InputStreamReader(server.getErrorStream());
        int ch;
        while ((ch = errorReader.read()) != -1) {
          System.out.print((char) ch);
        }

      } catch (IOException e) {
        e.printStackTrace();
      }
    }).start();

    new Thread(() -> {
      try {
        Reader reader = new InputStreamReader(server.getInputStream());
        int ch;
        while ((ch = reader.read()) != -1) {
          System.out.print((char) ch);
        }

      } catch (IOException e) {
        e.printStackTrace();
      }
    }).start();

    return server;
  }

  static void kill(Process process) {
    process.destroy();
    System.out.println("Program complete");
  }

  private static boolean status(String url) {
    RlcpEchoRequestBody body = new RlcpEchoRequestBody();
    RlcpEchoRequest rlcpEchoRequest = body.prepareRequest(url);
    try {
      rlcpEchoRequest.execute();
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public static GeneratingResult getGenerate(String algorithm, String url) {
    RlcpGenerateRequestBody body = new RlcpGenerateRequestBody(algorithm);
    RlcpGenerateRequest rlcpGenerateRequest = body.prepareRequest(url);
    RlcpGenerateResponse rlcpResponse = null;
    try {
      rlcpResponse = rlcpGenerateRequest.execute();
    } catch (Exception e) {
      e.printStackTrace();
    }
    GeneratingResult result = rlcpResponse.getBody().getGeneratingResult();
    System.out.println("GENERATE successfully with result: {\n" +
        "\tcode: \"" + result.getCode() + "\",\n" +
        "\ttext: \"" + result.getText() + "\",\n" +
        "\tinstraction: \"" + result.getInstructions() + "\"}");
    return result;
  }

  public static void main(String[] args) throws IOException, InterruptedException {
    Process process = starter();
    System.out.println(status("rlcp://localhost:3000"));
    Thread.sleep(2000);
    getGenerate("test", "rlcp://localhost:3000");
    kill(process);
  }

}
