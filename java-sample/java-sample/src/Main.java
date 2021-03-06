
import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;
import com.microsoft.azure.management.resources.ResourceManagementClient;
import com.microsoft.azure.management.resources.ResourceManagementService;
import com.microsoft.azure.management.resources.models.ResourceGroupExtended;
import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.management.configuration.ManagementConfiguration;

import javax.naming.ServiceUnavailableException;
import java.net.URI;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class Main {

    public static void main(String[] args) throws Exception {
        ResourceManagementClient client = Main.createResourceManagementClient();
        ArrayList<ResourceGroupExtended> groups = client.getResourceGroupsOperations().list(null).getResourceGroups();
        for (ResourceGroupExtended group : groups) {
            System.out.println(group.getName());
        }
    }

    protected static ResourceManagementClient createResourceManagementClient() throws Exception {
        Configuration config = createConfiguration();
        return ResourceManagementService.create(config);
    }

    public static Configuration createConfiguration() throws Exception {
        String baseUri = "https://management.azure.com";
        return ManagementConfiguration.configure(
                null,
                new URI(baseUri),
                "cbbdaed0-fea9-4693-bf0c-d446ac93c030",
                getAccessTokenFromUserCredentials().getAccessToken());
    }

    private static AuthenticationResult getAccessTokenFromUserCredentials() throws Exception {
        AuthenticationContext context = null;
        AuthenticationResult result = null;
        ExecutorService service = null;
        try {
            service = Executors.newFixedThreadPool(1);
            context = new AuthenticationContext("https://login.windows.net/" + "tenant_id",
                    false, service);
            ClientCredential cred = new ClientCredential("client_id",
                    "client_secret");
            Future<AuthenticationResult> future = context.acquireToken(
                    "https://management.azure.com/", cred, null);
            result = future.get();
        } finally {
            service.shutdown();
        }

        if (result == null) {
            throw new ServiceUnavailableException(
                    "authentication result was null");
        }
        return result;
    }
}
