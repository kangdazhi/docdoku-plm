/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2014 DocDoku SARL
 *
 * This file is part of DocDokuPLM.
 *
 * DocDokuPLM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDokuPLM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with DocDokuPLM.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.docdoku.server.converters.catia;

import com.docdoku.core.common.BinaryResource;
import com.docdoku.core.exceptions.*;
import com.docdoku.core.product.PartIteration;
import com.docdoku.core.product.PartIterationKey;
import com.docdoku.core.services.IDataManagerLocal;
import com.docdoku.core.services.IProductManagerLocal;
import com.docdoku.core.util.FileIO;
import com.docdoku.server.converters.CADConverter;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;

import javax.ejb.EJB;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;


@CatiaFileConverter
public class CatiaFileConverterImpl implements CADConverter{

    private static final String CONF_PROPERTIES="/com/docdoku/server/converters/catia/conf.properties";

    private static final Properties CONF = new Properties();

    @EJB
    private IProductManagerLocal productService;

    @EJB
    private IDataManagerLocal dataManager;

    static{
        InputStream inputStream = null;
        try {
            inputStream = CatiaFileConverterImpl.class.getResourceAsStream(CONF_PROPERTIES);
            CONF.load(inputStream);
        } catch (IOException e) {
            Logger.getLogger(CatiaFileConverterImpl.class.getName()).log(Level.WARNING, null, e);
        } finally {
            try{
                if(inputStream!=null){
                    inputStream.close();
                }
            }catch (IOException e){
                Logger.getLogger(CatiaFileConverterImpl.class.getName()).log(Level.FINEST,null, e);
            }
        }
    }

    @Override
    public File convert(PartIteration partToConvert, final BinaryResource cadFile, File tempDir) throws IOException, InterruptedException, UserNotActiveException, PartRevisionNotFoundException, WorkspaceNotFoundException, CreationException, UserNotFoundException, NotAllowedException, FileAlreadyExistsException, StorageException {
        String woExName = FileIO.getFileNameWithoutExtension(cadFile.getName());
        File tmpCadFile = new File(tempDir, cadFile.getName());
        File tmpDAEFile = new File(tempDir, woExName+".dae");
        String catPartConverter = CONF.getProperty("catPartConverter");

        Files.copy(new InputSupplier<InputStream>() {
            @Override
            public InputStream getInput() throws IOException {
                try {
                    return dataManager.getBinaryResourceInputStream(cadFile);
                } catch (StorageException e) {
                    Logger.getLogger(CatiaFileConverterImpl.class.getName()).log(Level.INFO, null, e);
                    throw new IOException(e);
                }
            }
        }, tmpCadFile);

        String[] args = {"sh", catPartConverter, tmpCadFile.getAbsolutePath() , tmpDAEFile.getAbsolutePath()};

        ProcessBuilder pb = new ProcessBuilder(args);
        Process process = pb.start();

        process.waitFor();

        // Convert to OBJ once converted to DAE
        if (process.exitValue() == 0 & tmpDAEFile.exists() && tmpDAEFile.length() > 0 ){

            String assimp = CONF.getProperty("assimp");
            String convertedFileName = FileIO.getFileNameWithoutExtension(tmpDAEFile.getAbsolutePath()) + ".obj";
            String[] argsOBJ = {assimp, "export", tmpDAEFile.getAbsolutePath(), convertedFileName};
            pb = new ProcessBuilder(argsOBJ);
            Process proc = pb.start();
            proc.waitFor();

            if (proc.exitValue() == 0) {
                return new File(convertedFileName);
            }
        }

        return null;
    }

    @Override
    public boolean canConvertToOBJ(String cadFileExtension) {
        return Arrays.asList("catpart").contains(cadFileExtension);
    }
}
