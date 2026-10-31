package application;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import model.*;
import persistence.*;

/** Explicit sample installation; ordinary catalog reads never import or change images. */
public final class CatalogSampleImages {
    private static String sample(CatalogItem item) {
        String name=item.name().trim().toLowerCase(Locale.ROOT).replaceAll("\\s+"," ");
        if(!item.product())return switch(name){
            case "wash", "wash only" -> "wash-only";
            case "dry", "dry only" -> "dry-only";
            case "full service", "wash, dry & fold", "wash dry fold" -> "full-service";
            case "fold", "fold only" -> "fold-only";
            default -> null;
        };
        return switch(item.id()){
            case "surf" -> "powder-pink";
            case "wings" -> "powder-blue";
            case "breeze" -> "liquid-teal";
            case "downy" -> "softener-yellow";
            default -> null;
        };
    }

    public static int applyMissing(RepositoryProvider.Catalog store) throws IOException {
        synchronized(LocalTransactions.LOCK) {
            security.LocalAuth.require(security.LocalAuth.Permission.CATALOG_EDIT);
            var next=new ArrayList<CatalogItem>();int changed=0;
            Path imports=DataLocation.resolve("images");
            for(CatalogItem item:store.items()){
                String photo=sample(item);
                if(!item.image().isBlank()||photo==null){next.add(item);continue;}
                Files.createDirectories(imports);
                Path temporary=Files.createTempFile(imports,".sample-import-",".png");
                String image;
                try(var input=CatalogSampleImages.class.getResourceAsStream("/resources/catalog-samples/"+photo+"-v1.png")){
                    if(input==null)throw new IOException("Bundled sample image is missing: "+photo);
                    Files.copy(input,temporary,StandardCopyOption.REPLACE_EXISTING);
                    image=ItemImages.importPhoto(temporary);
                }finally{Files.deleteIfExists(temporary);}
                next.add(new CatalogItem(item.id(),item.name(),item.description(),item.category(),item.unit(),item.price(),item.product(),image));changed++;
            }
            if(changed>0)store.save(next);
            return changed;
        }
    }

    public static void main(String[] args) throws Exception {
        if(args.length!=1)throw new IllegalArgumentException("Pass a new backup filename before installing samples.");
        var store=Repositories.catalog();
        if(store.items().stream().noneMatch(i->i.image().isBlank()&&sample(i)!=null)){System.out.println("No missing sample images to install. Existing photos were preserved.");return;}
        var console=System.console();
        if(console==null)throw new IOException("Sign in with an Owner or Manager account in an interactive terminal to install sample photos.");
        char[] master=console.readPassword("Master key: ");String username=console.readLine("Username: ");char[] password=console.readPassword("Password: ");
        try{security.LocalAuth.unlock(master);security.LocalAuth.signIn(username,password);security.LocalAuth.require(security.LocalAuth.Permission.CATALOG_EDIT);}
        finally{java.util.Arrays.fill(master,'\0');java.util.Arrays.fill(password,'\0');}
        Path source=DataLocation.resolve("catalog.properties"),backup=Path.of(args[0]).toAbsolutePath();
        Files.createDirectories(backup.getParent());
        if(Files.exists(source))Files.copy(source,backup);
        else Files.writeString(backup,"No saved catalog existed before this sample installation.\n",StandardOpenOption.CREATE_NEW);
        int changed=applyMissing(store);
        System.out.println("Installed "+changed+" sample photos. Existing photos, names, prices and quantities were preserved.");
        System.out.println("Catalog backup: "+backup);
    }
    private CatalogSampleImages(){}
}
