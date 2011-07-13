package org.fedoraproject.eclipse.packager.koji.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.apache.ws.commons.util.Base64;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.fedoraproject.eclipse.packager.SourcesFile;
import org.fedoraproject.eclipse.packager.api.FedoraPackagerCommand;
import org.fedoraproject.eclipse.packager.api.errors.CommandListenerException;
import org.fedoraproject.eclipse.packager.api.errors.CommandMisconfiguredException;
import org.fedoraproject.eclipse.packager.koji.KojiText;
import org.fedoraproject.eclipse.packager.koji.api.errors.KojiHubClientException;
import org.fedoraproject.eclipse.packager.koji.api.errors.KojiHubClientLoginException;
import org.fedoraproject.eclipse.packager.koji.api.errors.TagSourcesException;
import org.fedoraproject.eclipse.packager.koji.api.errors.UnpushedChangesException;
import org.apache.commons.codec.binary.Hex;

/**
 * Command for uploading an SRPM to Koji.
 *
 */
public class KojiUploadSRPMCommand extends FedoraPackagerCommand<BuildResult> {
		/**
		 * Unique ID for this command.
		 */
		public final static String ID = "KojiUploadSRPMCommand"; //$NON-NLS-1$
		private File srpm;
		private FileInputStream fis;
		private String remotePath;
		private IKojiHubClient client;
		
		@Override
		public BuildResult call(IProgressMonitor monitor) 
				throws CommandMisconfiguredException, KojiHubClientException, 
				KojiHubClientLoginException, CommandListenerException{
			try {
				callPreExecListeners();
			} catch (CommandListenerException e) {
				if (e.getCause() instanceof CommandMisconfiguredException) {
					// explicitly throw the specific exception
					throw (CommandMisconfiguredException)e.getCause();
				}
				throw e;
			}
			try {
				fis = new FileInputStream(srpm);
				monitor.beginTask(NLS.bind(
						KojiText.KojiUploadSRPMJob_KojiUpload,
						srpm.getName()), fis.available());
			} catch (FileNotFoundException e){
				throw new CommandMisconfiguredException(NLS.bind(
						KojiText.KojiUploadSRPMCommand_FileNotFound, srpm.getName()));
			} catch (IOException e) {
				throw new CommandMisconfiguredException(NLS.bind(
						KojiText.KojiUploadSRPMCommand_CouldNotRead , srpm.getName()));
			}
			client.login();
			
			String srpmName = srpm.getName();
			byte[] readData = null;
			boolean success = true;
			
			try {
				
				int chunkSize = Math.min(fis.available(), 1000000);
				int chunkOffset = 0;
				String md5sum = null;
				String base64 = null;
				while (chunkSize > 0){
					readData = new byte[chunkSize];
					fis.read(readData);
					md5sum = Hex.encodeHexString(
							MessageDigest.getInstance("MD5").digest(readData)); //$NON-NLS-1$
					base64 = Base64.encode(readData);
					success = (success && client.uploadFile(remotePath, srpmName, chunkSize, md5sum, chunkOffset, base64));
					monitor.worked(chunkSize);
					chunkOffset += chunkSize;
					chunkSize = Math.min(fis.available(), 1000000);
				}
			} catch (IOException e1) {
				throw new CommandMisconfiguredException(NLS.bind(
						KojiText.KojiUploadSRPMCommand_CouldNotRead , srpm.getName()));
			} catch (NoSuchAlgorithmException e) {
				// should not occur
				throw new CommandMisconfiguredException(KojiText.KojiUploadSRPMCommand_NoMD5);
			}
			BuildResult result = new BuildResult();
			if (success){
				result.setSuccessful();
			}
			return result;
		}
		
		/**
		 * @param srpmPath The path of the rpm to use.
		 * @return This command.
		 */
		public KojiUploadSRPMCommand setSRPM(String srpmPath){
			srpm = new File(srpmPath);
			return this;
		}
		
		/**
		 * @param remotePath The path on the server to upload the SRPM to.
		 * @return This command.
		 */
		public KojiUploadSRPMCommand setRemotePath(String remotePath){
			this.remotePath = remotePath;
			return this;
		}
		
		/**
		 * @param client The client used when connecting to koji.
		 * @return This command.
		 */
		public KojiUploadSRPMCommand setKojiClient(IKojiHubClient client){
			this.client = client;
			return this;
		}

		@Override
		protected void checkConfiguration()
				throws CommandMisconfiguredException {
			if (client == null){
				throw new CommandMisconfiguredException(NLS.bind(
						KojiText.KojiBuildCommand_configErrorNoClient,
						this.projectRoot.getProductStrings().getBuildToolName()));
			} if (srpm == null){
				throw new CommandMisconfiguredException(
						KojiText.KojiUploadSRPMCommand_NoSRPM);
			} if (!srpm.getName().endsWith(".src.rpm")){ //$NON-NLS-1$
				throw new CommandMisconfiguredException(NLS.bind(
						KojiText.KojiUploadSRPMCommand_InvalidSRPM ,
						srpm.getName()));
			} if (remotePath == null){
				throw new CommandMisconfiguredException(
						KojiText.KojiUploadSRPMCommand_NoUploadPath);
			}
			
		}
}
