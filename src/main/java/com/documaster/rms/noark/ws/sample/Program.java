package com.documaster.rms.noark.ws.sample;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.documaster.idp.client.ClientIDP;
import com.documaster.idp.client.HttpClientIDP;
import com.documaster.idp.client.OpenIDConnectScope;

import com.documaster.rms.noark.ws.client.NoarkClient;

import com.documaster.rms.noark.ws.client.RmsClient;
import com.documaster.rms.noark.ws.client.bsm.BusinessSpecificMetadataInfo;
//import com.documaster.rms.noark.ws.client.bsm.MetadataFieldInfo;
//import com.documaster.rms.noark.ws.client.bsm.MetadataGroupInfo;
import com.documaster.rms.noark.ws.client.codelist.CodeList;
import com.documaster.rms.noark.ws.client.codelist.CodeValue;
import com.documaster.rms.noark.ws.client.query.Order;
import com.documaster.rms.noark.ws.client.query.QueryResponse;
import com.documaster.rms.noark.ws.client.transaction.TransactionResponse;
import com.documaster.rms.noark.ws.constants.AdministrativEnhet;
import com.documaster.rms.noark.ws.constants.Dokumenttype;
import com.documaster.rms.noark.ws.constants.Journalposttype;
import com.documaster.rms.noark.ws.constants.Korrespondanseparttype;
import com.documaster.rms.noark.ws.constants.Mappetype;
import com.documaster.rms.noark.ws.constants.Skjerming;
import com.documaster.rms.noark.ws.constants.TilknyttetRegistreringSom;
import com.documaster.rms.noark.ws.constants.Variantformat;
import com.documaster.rms.noark.ws.noarkentities.Arkiv;
import com.documaster.rms.noark.ws.noarkentities.Arkivdel;
import com.documaster.rms.noark.ws.noarkentities.Arkivskaper;
import com.documaster.rms.noark.ws.noarkentities.Basisregistrering;
import com.documaster.rms.noark.ws.noarkentities.Dokument;
import com.documaster.rms.noark.ws.noarkentities.Dokumentfil;
import com.documaster.rms.noark.ws.noarkentities.Dokumentversjon;
import com.documaster.rms.noark.ws.noarkentities.EksternId;
import com.documaster.rms.noark.ws.noarkentities.Journalpost;
import com.documaster.rms.noark.ws.noarkentities.Klasse;
import com.documaster.rms.noark.ws.noarkentities.Klassifikasjonssystem;
import com.documaster.rms.noark.ws.noarkentities.Korrespondansepart;
import com.documaster.rms.noark.ws.noarkentities.Mappe;
import com.documaster.rms.noark.ws.noarkentities.NoarkEntity;
import com.documaster.rms.noark.ws.noarkentities.Noekkelord;
import com.documaster.rms.noark.ws.noarkentities.Saksmappe;
import com.documaster.rms.noark.ws.noarkentities.Sakspart;
import com.documaster.rms.noark.ws.noarkentities.bsm.BsmFieldValues;
import com.documaster.rms.noark.ws.noarkentities.bsm.BsmFieldsMap;
import com.documaster.rms.noark.ws.noarkentities.bsm.BsmGroupsMap;
import com.fasterxml.jackson.core.JsonProcessingException;

public class Program {

	private static NoarkClient client;
	private static ClientIDP idpClient;
	private static String testDoc;

	public static void main(String... args) throws IOException {

		// A sample set of configuration settings would look like this:
		//   -idpaddr https://idp.dev.documaster.tech/oauth2
		//   -clientid clientID
		//   -clientsecret clientSecret
		//   -username admin
		//   -password admin
		//   -addr https://rms.dev.documaster.tech:8083
		//   -cert "/path/to/file.jks"
		//   -certpass changeit
		//   -testdoc "/path/to/sample/file.txt"
		Options options = parseCommandLineArguments(args);

//		System.setProperty("javax.net.ssl.keyStore", options.getCertificatePath());
//		System.setProperty("javax.net.ssl.keyStorePassword", options.getCertificatePass());

		initializeSample(options);

		print("Start of sample");
		print();

		journalingSample();
		archiveSample();
		businessSpecificMetadataSample();

		print();
		print("End of sample");
	}

	private static Options parseCommandLineArguments(String... args) {

		Map<String, String> parameters = new HashMap<>();

		Iterator<String> iter = Arrays.stream(args).iterator();

		String lastArgument = null;

		// A simple command-line argument parser
		while (iter.hasNext()) {

			String value = iter.next();

			if (value.startsWith("-")) {

				lastArgument = value.substring(1);
				parameters.put(value.substring(1), null);

			} else {

				if (lastArgument == null) {

					throw new IllegalArgumentException("Found a value, but not a parameter to link it to");
				}

				// If multiple values are passed for a parameter, only the last one will be taken into account;
				if (parameters.put(lastArgument, value) != null) {

					throw new IllegalArgumentException("Multiple values passed for parameter " + lastArgument);
				}
			}
		}

		Options opts = new Options(
				parameters.get("idpaddr"),
				parameters.get("clientid"),
				parameters.get("clientsecret"),
				parameters.get("username"),
				parameters.get("password"),
				parameters.get("addr"),
				parameters.get("cert"),
				parameters.get("certpass"),
				parameters.get("testdoc"));

		return opts;
	}

	/**
	 * Using the Noark 5 web services requires providing a valid access token.
	 * The way this token is obtained depends on the system implementing the services.
	 * This sample code obtains the token from the Documaster's identity provider service
	 * with the help of a designated Documaster IDP client.
	 * If the Noark client is used in the context of an application that has access to a web browser,
	 * we strongly recommend choosing the Oauth2 Authorization Code Grant Flow supported for obtaining
	 * access tokens.
	 */
	private static void initializeSample(Options options) {

		// Initialize an IDP client and request an authorization token
		initIdpClient(options);

		String accessToken = idpClient.getTokenWithPasswordCredentials(
				options.getClientId(),
				options.getClientSecret(),
				options.getUsername(),
				options.getPassword(),
				OpenIDConnectScope.OPENID.getName()
		).getAccessToken();

		// Initialize a Noark client
		initClient(options);
		client.setAuthToken(accessToken);

		testDoc = options.getTestDoc();
	}

	private static void initIdpClient(Options options) {

		idpClient = new HttpClientIDP(options.getIdpServerAddress());
	}

	private static void initClient(Options options) {

		client = new NoarkClient(new RmsClient(options.getServerAddress()));
	}

	private static void journalingSample() throws IOException {

		print();
		print("Journaling example");

		Klasse newKlasseT = new Klasse("01", "Tilbud");
		print("%s", newKlasseT);

//		Klassifikasjonssystem newKlassifikasjonssystem2 = new Klassifikasjonssystem("Barnehage");
//
//		TransactionResponse transactionResponse2 = client.transaction()
//				.save(newKlasseT)
//				.link(newKlasseT.linkKlassifikasjonssystem(newKlassifikasjonssystem2))
//				.commit();
//		print("    Updated Arkiv: Id=%s, Beskrivelse=%s", transactionResponse2.getSaved());


		// Create a new Mappe
//		Mappe newMappe = new Mappe("Barnehage Tilbud");
//
//		newMappe.setBeskrivelse("Mappe Beskrivelse");
//
//		TransactionResponse transactionResponse3 = client.transaction()
//				.save(newMappe)
//				.commit();
//
//		Mappe mappe = (Mappe) transactionResponse3.getSaved().get(newMappe.getId());
//		print("    Created Mappe: Id=%s, Tittel=%s", mappe.getId(), mappe.getTittel());

		Arkivskaper newArkivskaper2 = new Arkivskaper("B7-23-W5", "John Smith");
		Arkiv newArkiv2 = new Arkiv("Arkiv");

		TransactionResponse transactionResponse = client.transaction()
				.save(newArkiv2)
				.save(newArkivskaper2)
				.link(newArkiv2.linkArkivskaper(newArkivskaper2))
				.commit();

		print("Updated Arkiv: Id=%s, Beskrivelse=%s", transactionResponse.getSaved());

		// Create a new Arkiv with an Arkivskaper
		// When new objects are initialized, a temporary Id is assigned to them.
		Arkivskaper newArkivskaper = new Arkivskaper("B7-23-W5", "John Smith");
		Arkiv newArkiv = new Arkiv("Arkiv");

		TransactionResponse transactionResponse2 = client.transaction()
				.save(newArkiv)
				.save(newArkivskaper)
				.link(newArkiv.linkArkivskaper(newArkivskaper))
				.commit();

		print("Updated Arkiv: Id=%s, Beskrivelse=%s", transactionResponse2.getSaved());

		// When the transaction is committed, the transaction response contains a map with saved objects.
		// One can access the saved Arkiv by providing its temporary Id as a key to the map.
		// Notice that arkiv.getId() is the permanent Id of the Arkiv.
		Arkiv arkiv = (Arkiv) transactionResponse.getSaved().get(newArkiv.getId());
		print(
				"    Created Arkiv: Id=%s, Tittel=%s, OpprettetDato=%s", arkiv.getId(), arkiv.getTittel(),
				arkiv.getOpprettetDato());

		// Update the description of the Arkiv and create a new Arkivdel in it
		// Create a new Klassifikasjonssystem with one Klasse
		// Set the new Klassifikasjonssystem as the primary Klassifikasjonssystem for the Arkivdel
		arkiv.setBeskrivelse("Barnehage Arkiv");

		Arkivdel newArkivdel = new Arkivdel("2007/8");
		Klassifikasjonssystem newKlassifikasjonssystem = new Klassifikasjonssystem("Barnehage");
		Klasse newKlasse = new Klasse("01", "Tilbud");

		transactionResponse = client.transaction()
				.save(arkiv)
				.save(newArkivdel)
				.link(newArkivdel.linkArkiv(arkiv))
				.save(newKlassifikasjonssystem)
				.link(newArkivdel.linkPrimaerKlassifikasjonssystem(newKlassifikasjonssystem))
				.save(newKlasse)
				.link(newKlasse.linkKlassifikasjonssystem(newKlassifikasjonssystem))
				.commit();

		arkiv = (Arkiv) transactionResponse.getSaved().get(arkiv.getId());
		print("    Updated Arkiv: Id=%s, Beskrivelse=%s", arkiv.getId(), arkiv.getBeskrivelse());

		Arkivdel arkivdel = (Arkivdel) transactionResponse.getSaved().get(newArkivdel.getId());
		print("    Created Arkivdel: Id=%s, Tittel=%s", arkivdel.getId(), arkivdel.getTittel());

		String klassifikasjonssystemId = transactionResponse.getSaved().get(newKlassifikasjonssystem.getId()).getId();
		String klasseId = transactionResponse.getSaved().get(newKlasse.getId()).getId();

		// To screen an Arkivdel we should first search the system for available screening codes
		CodeList screeningCodesList = client.codeLists(null, "skjerming").get(0);

		print("    Screening codes:");
		for (CodeValue code : screeningCodesList.getValues()) {

			print("        Code=%s", code.getCode());
		}

		if (screeningCodesList.getValues().isEmpty()) {

			print("Cannot screen Arkivdel because there are not available values in the Skjerming code list!");

		} else {

			// Screen the Arkivdel
			CodeValue screeningCode = screeningCodesList.getValues().get(0);

			arkivdel.setSkjerming(new Skjerming(screeningCode.getCode()));
			client.transaction()
					.save(arkivdel)
					.commit();
		}

		// Find the Arkivdel by id
		// By default the service will return null values for all screened fields of screened objects
		// To see the values of screened fields call SetPublicUse(false)
		QueryResponse<Arkivdel> queryResults = client.query(Arkivdel.class, "id=@arkivdelId", 10)
				.addQueryParam("@arkivdelId", arkivdel.getId())
				.execute();
		print("    Found %s Arkivdel object(s) with Id %s", queryResults.getResults().size(), arkivdel.getId());

		boolean isArkivdelScreened = !screeningCodesList.getValues().isEmpty();

		// Print a screened field:
		arkivdel = queryResults.getResults().iterator().next();
		print("    Tittel of Arkivdel is %s masked: %s", isArkivdelScreened ? "" : "not", arkivdel.getTittel());

		// For convenience, objects in query and transaction responses contain the id's of any-to-one reference fields
		print("    Arkivdel.RefArkiv: %s", arkivdel.getRefArkiv());
		print("    Arkivdel.RefPrimaerKlassifikasjonssystem: %s", arkivdel.getRefPrimaerKlassifikasjonssystem());

		// Create two other Klassifikasjonssystem objects and link them to the Arkivdel as secondary Klassifikasjonssystem
		Klassifikasjonssystem sekundaerKlassifikasjonssystemSkole = new Klassifikasjonssystem("Skole");
		Klasse klasseInSekundaerKlassifikasjonssystemSkole = new Klasse("07", "Report");
		Klassifikasjonssystem sekundaerKlassifikasjonssystem2 = new Klassifikasjonssystem("EOP");

		transactionResponse = client.transaction()
				.save(sekundaerKlassifikasjonssystemSkole)
				.save(klasseInSekundaerKlassifikasjonssystemSkole)
				.link(sekundaerKlassifikasjonssystemSkole.linkKlasse(klasseInSekundaerKlassifikasjonssystemSkole))
				.save(sekundaerKlassifikasjonssystem2)
				.link(arkivdel.linkSekundaerKlassifikasjonssystem(
						sekundaerKlassifikasjonssystemSkole,
						sekundaerKlassifikasjonssystem2))
				.commit();

		// We need the id of the saved Klasse for the next transactions
		String sekundaerKlasseId =
				transactionResponse.getSaved().get(klasseInSekundaerKlassifikasjonssystemSkole.getId()).getId();

		// Create a new Saksmappe in the Arkivdel
		// The new Saksmappe needs to have a Klasse in the primary Klassifikasjonssystem of the Arkivdel
		// Also link the Saksmappe to a secondary Klasse

		List<CodeList> administrativeUnits = client.codeLists(null, "administrativEnhet");

		if (administrativeUnits.isEmpty()) {

			print("No administrative units have been found in the system. Cannot create Saksmappe.");
			return;
		}

		String administrativeUnit = administrativeUnits.get(0).getValues().get(0).getCode();

		Saksmappe newSaksmappe = new Saksmappe("Tilbud (Smith, John)", new AdministrativEnhet(administrativeUnit));
		Sakspart newSakspart = new Sakspart("Alice", "internal");

		Map<String, NoarkEntity> savedObjects = client.transaction()
				.save(newSaksmappe)
				.link(newSaksmappe.linkArkivdel(arkivdel))
				.link(newSaksmappe.linkPrimaerKlasse(klasseId))
				.link(newSaksmappe.linkSekundaerKlasse(sekundaerKlasseId))
				.save(newSakspart)
				.link(newSaksmappe.linkSakspart(newSakspart))
				.commit()
				.getSaved();

		Saksmappe saksmappe = (Saksmappe) savedObjects.get(newSaksmappe.getId());
		print("    Created Saksmappe: Id=%s, Saksdato: %s", saksmappe.getId(), saksmappe.getSaksdato());

		// Create another Klasse
		// Unlink the Saksmappe from its Klasse and link it to the new Klasse
		Klasse anotherKlasse = new Klasse("02", "Klage");

		client.transaction()
				.save(anotherKlasse)
				.link(anotherKlasse.linkKlassifikasjonssystem(klassifikasjonssystemId))
				.unlink(saksmappe.unlinkPrimaerKlasse(klasseId))
				.link(saksmappe.linkPrimaerKlasse(anotherKlasse))
				.commit();
		print(
				"    Unlinked Saksmappe wiht Id %s from Klasse '%s' and linked it to Klasse '%s'", saksmappe.getId(),
				newKlasse.getTittel(), anotherKlasse.getTittel());

		// Find all available codes for journalstatus in Journalpost
		CodeList journalstatusCodeList = client.codeLists("Journalpost", "journalstatus").get(0);
		print("    CodeList list for %s.%s:", journalstatusCodeList.getType(), journalstatusCodeList.getField());
		for (CodeValue code : journalstatusCodeList.getValues()) {

			print("        Code=%s, Name=%s", code.getCode(), code.getName());
		}

		// Create a new Journalpost in the Saksmappe
		// Create an EksternId object and link it to the Journalpost
		// Create a new Korrespondansepart and link it to the Journalpost
		// Create a Noekkelord (keyword) object and link it to the Journalpost
		Journalpost newJournalpost =
				new Journalpost("Tilbud (Smith, John, Godkjent)", Journalposttype.UTGAAENDE_DOKUMENT);
		newJournalpost.setJournalaar(2007);
		newJournalpost.setJournalsekvensnummer(46);

		EksternId newEksternId = new EksternId("External System", UUID.randomUUID().toString());
		Korrespondansepart newKorrespondansepart =
				new Korrespondansepart(Korrespondanseparttype.INTERN_MOTTAKER, "John Smith");
		Noekkelord newNoekkelord = new Noekkelord("keyword");

		savedObjects = client.transaction()
				.save(newJournalpost)
				.link(newJournalpost.linkMappe(saksmappe))
				.save(newEksternId)
				.link(newJournalpost.linkEksternId(newEksternId))
				.save(newKorrespondansepart)
				.link(newJournalpost.linkKorrespondansepart(newKorrespondansepart))
				.save(newNoekkelord)
				.link(newNoekkelord.linkRegistrering(newJournalpost))
				.commit()
				.getSaved();

		Journalpost journalPost = (Journalpost) savedObjects.get(newJournalpost.getId());
		print(
				"    Created Journalpost: Id=%s, Tittel=%s, Journalstatus=%s", journalPost.getId(),
				journalPost.getTittel(),
				journalPost.getJournalstatus().getCode());

		//Find the Journalpost by the eksternID value
		QueryResponse<Journalpost> journalpostQueryResults =
				client.query(Journalpost.class, "refEksternId.eksternID=@eksternId", 10)
						.addQueryParam("@eksternId", newEksternId.getEksternID())
						.execute();
		print("    Found %s Journalpost objects with eksternID %s", journalpostQueryResults.getResults().size(),
				newEksternId.getEksternID());

		//Upload a file
		Dokumentfil dokumentfil;
		try (InputStream inputStream = Files.newInputStream(Paths.get(testDoc))) {

			dokumentfil = client.upload(inputStream, "godkjenning.pdf");
		}

		print("    Uploaded file %s", testDoc);

		// Get available values for the Dokumenttype code list
		CodeList dokumenttypeList = client.codeLists("Dokument", "dokumenttype").get(0);
		if (dokumenttypeList.getValues().isEmpty()) {

			print("Cannot create an instance of Dokumentype "
					+ "because there are no available values in the Dokumenttype code list!");
		}

		// Create a new Dokument and Dokumentversjon using the uploaded file
		Dokument newDokument = new Dokument("Tilbud (Smith, John, Godkjent)", TilknyttetRegistreringSom.HOVEDDOKUMENT);

		if (!dokumenttypeList.getValues().isEmpty()) {

			String dokumentTypeCode = dokumenttypeList.getValues().get(0).getCode();
			newDokument.setDokumenttype(new Dokumenttype(dokumentTypeCode));
		}

		Dokumentversjon newDokumentversjon = new Dokumentversjon(Variantformat.PRODUKSJONSFORMAT, ".pdf", dokumentfil);

		savedObjects = client.transaction()
				.save(newDokument)
				.link(newDokument.linkRegistrering(journalPost))
				.save(newDokumentversjon)
				.link(newDokumentversjon.linkDokument(newDokument))
				.commit()
				.getSaved();

		Dokumentversjon dokumentversjon = (Dokumentversjon) savedObjects.get(newDokumentversjon.getId());
		print(
				"    Created Dokumentversjon: Id=%s, Versjonsnummer: %s, Filstoerrelse: %s", dokumentversjon.getId(),
				dokumentversjon.getVersjonsnummer(), dokumentversjon.getFilstoerrelse());

		// Download the Dokumentversjon file
		Path downloadPath = Files.createTempFile("ws-client-sample-", "-test-file");
		try (OutputStream outputStream = Files.newOutputStream(downloadPath)) {

			client.download(dokumentversjon.getDokumentfil(), outputStream);
		}

		print("    Downloaded file %s", downloadPath);

		// Find all dokument objects in a Saksmappe called "Tilbud (Smith, John)"
		// Results should be ordered by creation date in descending order
		QueryResponse<Dokument> queryResponse =
				client.query(Dokument.class, "refRegistrering.refMappe.tittel=@saksmappeTittel", 50)
						.addQueryParam("@saksmappeTittel", "Tilbud (Smith, John)")
						.addSortOrder("opprettetDato", Order.DESCENDING)
						.execute();

		print(
				"    Query returned %s Dokument objects in Saksmappe objects called 'Tilbud (Smith, John)'",
				queryResponse.getResults().size());
		print("    More results available: %s", queryResponse.getHasMore());

		// Delete the DokumentVersjon by type, id, and version
		client.transaction().delete(dokumentversjon).commit();
		print("    Deleted Dokumentversjon with Id %s", dokumentversjon.getId());

		print("End of Journaling example");
	}


	private static void archiveSample() throws IOException {

		print();
		print("Archive example");

		//Create a new Arkiv with an Arkivskaper
		//Create a new Arkivdel in the Arkiv
		Arkivskaper newArkivskaper = new Arkivskaper("B7-23-W5", "John Smith");
		Arkiv newArkiv = new Arkiv("Arkiv");
		Arkivdel newArkivdel = new Arkivdel("2007/8");

		TransactionResponse transactionResponse = client.transaction()
				.save(newArkiv)
				.save(newArkivskaper)
				.save(newArkivdel)
				.link(newArkiv.linkArkivskaper(newArkivskaper))
				.link(newArkivdel.linkArkiv(newArkiv))
				.commit();

		Arkiv arkiv = (Arkiv) transactionResponse.getSaved().get(newArkiv.getId());
		print(
				"    Created Arkiv: Id=%s, Arkivstatus=%s, OpprettetDato=%s", arkiv.getId(),
				arkiv.getArkivstatus().getCode(), arkiv.getOpprettetDato());

		Arkivdel arkivdel = (Arkivdel) transactionResponse.getSaved().get(newArkivdel.getId());
		print(
				"    Created Arkivdel: Id=%s, Arkivdelstatus=%s", arkivdel.getId(),
				arkivdel.getArkivdelstatus().getCode());

		// Get all available values for the Mappetype code list
		CodeList mappetypeList = client.codeLists("Mappe", "mappetype").get(0);
		if (mappetypeList.getValues().isEmpty()) {

			print("Cannot create a Mappetype because there are no available values in the Mappetype code list!");
		}

		// Create a new Mappe
		Mappe newMappe = new Mappe("Barnehage Tilbud");

		newMappe.setBeskrivelse("Mappe Beskrivelse");

		if (!mappetypeList.getValues().isEmpty()) {

			String mappetypeCode = mappetypeList.getValues().get(0).getCode();
			newMappe.setMappetype(new Mappetype(mappetypeCode));
		}

		transactionResponse = client.transaction()
				.save(newMappe)
				.link(newMappe.linkArkivdel(arkivdel))
				.commit();

		Mappe mappe = (Mappe) transactionResponse.getSaved().get(newMappe.getId());
		print("    Created Mappe: Id=%s, Tittel=%s", mappe.getId(), mappe.getTittel());

		//C reate a child Mappe in the Mappe
		Mappe newBarnMappe = new Mappe("Tilbud (Smith, John)");

		Map<String, NoarkEntity> savedObjects = client.transaction()
				.save(newBarnMappe)
				.link(newBarnMappe.linkForelderMappe(mappe))
				.commit()
				.getSaved();

		Mappe barnMappe = (Mappe) savedObjects.get(newBarnMappe.getId());
		print("    Created a new Mappe (Id=%s, Tittel=%s) in Mappe with Id %s", barnMappe.getId(),
				barnMappe.getTittel(), mappe.getId());

		// Find all children of the Mappe
		QueryResponse<Mappe> queryResults = client.query(Mappe.class, "refForelderMappe.id=@forelderMappeId", 10)
				.addQueryParam("@forelderMappeId", mappe.getId())
				.execute();

		print("    Found %s Mappe objects in Mappe with Id %s", queryResults.getResults().size(), mappe.getId());

		// Create a new Basisregistrering in the child Mappe
		// Link one Korrespondansepart to the Basisregistrering
		Basisregistrering newBasisregistrering = new Basisregistrering("Tilbud (Smith, John, Godkjent)");
		Korrespondansepart newKorrespondansepart =
				new Korrespondansepart(Korrespondanseparttype.MOTTAKER, "John Smith");

		savedObjects = client.transaction()
				.save(newBasisregistrering)
				.save(newKorrespondansepart)
				.link(newBasisregistrering.linkMappe(barnMappe))
				.link(newBasisregistrering.linkKorrespondansepart(newKorrespondansepart))
				.commit()
				.getSaved();

		Basisregistrering basisregistrering = (Basisregistrering) savedObjects.get(newBasisregistrering.getId());
		print(
				"    Created Basisregistrering: Id=%s, Tittel=%s", basisregistrering.getId(),
				basisregistrering.getTittel());

		// Upload a file
		Dokumentfil dokumentfil;
		try (InputStream inputStream = Files.newInputStream(Paths.get(testDoc))) {

			dokumentfil = client.upload(inputStream, "godkjenning.pdf");
		}
		print("    Uploaded file %s", testDoc);

		// Get available values for the Dokumenttype code list
		CodeList dokumenttypeList = client.codeLists("Dokument", "dokumenttype").get(0);

		if (dokumenttypeList.getValues().isEmpty()) {

			print("Cannot create an instance of Dokumenttype "
					+ "because there are no available values in the Dokumenttype code list!");
		}

		// Create a new Dokument and Dokumentversjon using the uploaded file
		// Link the Dokument to the Basisregistrering
		Dokument newDokument = new Dokument("Tilbud (Smith, John, Godkjent)", TilknyttetRegistreringSom.HOVEDDOKUMENT);

		if (!dokumenttypeList.getValues().isEmpty()) {

			String dokumenttypeCode = dokumenttypeList.getValues().get(0).getCode();
			newDokument.setDokumenttype(new Dokumenttype(dokumenttypeCode));
		}

		Dokumentversjon newDokumentversjon = new Dokumentversjon(Variantformat.PRODUKSJONSFORMAT, ".pdf", dokumentfil);

		savedObjects = client.transaction()
				.save(newDokument)
				.link(newDokument.linkRegistrering(basisregistrering))
				.save(newDokumentversjon)
				.link(newDokumentversjon.linkDokument(newDokument))
				.commit()
				.getSaved();

		Dokumentversjon dokumentversjon = (Dokumentversjon) savedObjects.get(newDokumentversjon.getId());
		print(
				"    Created Dokumentversjon: Id=%s, Versjonsnummer: %s, Filstoerrelse: %s", dokumentversjon.getId(),
				dokumentversjon.getVersjonsnummer(), dokumentversjon.filstoerrelse);

		print("End of Archive example");
	}

	private static void businessSpecificMetadataSample() throws JsonProcessingException {

		print();
		print("Business-specific metadata example");

		String groupId = "appr-03";

		// Get the business-specific metadata registry for a group named "appr-03"
		// It is expected that the group exists and has at least one field
		BusinessSpecificMetadataInfo metadataInfo = client.bsmRegistry(groupId, null);

		// Print the registry for this group
		// Also find one string-type, long-type and double-type field
		String stringFieldId = null;
		String doubleFieldId = null;
		String longFieldId = null;

//		for (MetadataGroupInfo groupInfo : metadataInfo.getGroups()) {
//
//			print("    GroupInfo: GroupId=%s, GroupName=%s", groupInfo.getGroupId(), groupInfo.getGroupName());
//
//			for (MetadataFieldInfo fieldInfo : groupInfo.getFields()) {
//
//				print(
//						"        FieldInfo: FieldId=%s, FieldType=%s, FieldName=%s", fieldInfo.getFieldId(),
//						fieldInfo.getFieldType(), fieldInfo.getFieldName());
//
//				if (fieldInfo.getFieldType() == FieldType.STRING && stringFieldId == null) {
//
//					stringFieldId = fieldInfo.getFieldId();
//				}
//
//				if (fieldInfo.getFieldType() == FieldType.DOUBLE && doubleFieldId == null) {
//
//					doubleFieldId = fieldInfo.getFieldId();
//				}
//
//				if (fieldInfo.getFieldType() == FieldType.LONG && longFieldId == null) {
//
//					longFieldId = fieldInfo.getFieldId();
//				}
//			}
//		}

		// Create an Arkiv, Arkivdel and one Mappe
		// Set VirksomhetsspesifikkeMetadata for the Mappe
		Arkivskaper arkivskaper = new Arkivskaper("B67", "Jack Smith");
		Arkiv arkiv = new Arkiv("Arkiv - VirksomhetsspesifikkeMetadata Example");
		Arkivdel arkivdel = new Arkivdel("Arkivdel - VirksomhetsspesifikkeMetadata Example");

		Mappe mappe = new Mappe("Mappe with VirksomhetsspesifikkeMetadata");

		// Add three meta-data fields to the Mappe:
		if (stringFieldId != null) {

			mappe.getVirksomhetsspesifikkeMetadata()
					.addBsmFieldValues(groupId, stringFieldId, "value 1", "string value 2");
		}

		if (doubleFieldId != null) {

			mappe.getVirksomhetsspesifikkeMetadata().addBsmFieldValues(groupId, doubleFieldId, 5.63, 6.7);
		}

		if (longFieldId != null) {

			mappe.getVirksomhetsspesifikkeMetadata().addBsmFieldValues(groupId, longFieldId, 167907000L);
		}

		TransactionResponse transactionResponse = client.transaction()
				.save(arkiv)
				.save(arkivskaper)
				.link(arkiv.linkArkivskaper(arkivskaper))
				.save(arkivdel)
				.link(arkivdel.linkArkiv(arkiv))
				.save(mappe)
				.link(mappe.linkArkivdel(arkivdel))
				.commit();

		// Get the saved Mappe
		mappe = (Mappe) transactionResponse.getSaved().get(mappe.getId());

		// Print the VirksomhetsspesifikkeMetadata of the Mappe
		BsmGroupsMap groupsMap = mappe.getVirksomhetsspesifikkeMetadata();
		for (String gId : groupsMap.keySet()) {

			BsmFieldsMap fieldsMap = mappe.getVirksomhetsspesifikkeMetadata().get(gId);

			for (String fieldId : fieldsMap.keySet()) {

				BsmFieldValues values = fieldsMap.get(fieldId);
				print(
						"    GroupId=%s, FieldId=%s, ValueType=%s, Values=%s", gId, fieldId, values.getType(),
						values.getValues());
			}
		}

		// Update the VirksomhetsspesifikkeMetadata of the Mappe

		// Add one more string value to the string field
		if (stringFieldId != null) {

			// To add a new field value, simply add it to the set of values of the particular field
			// Use the "AddBsmFieldValues" method, if you want to override the existing set of values with a new one
			mappe.getVirksomhetsspesifikkeMetadata().get(groupId).get(stringFieldId).getValues().add("string value 3");
		}

		// Remove one of the values of the double field
		if (doubleFieldId != null) {

			mappe.getVirksomhetsspesifikkeMetadata().deleteBsmFieldValue(groupId, doubleFieldId, 5.63);
		}

		// Completely remove the long field
		if (longFieldId != null) {

			mappe.getVirksomhetsspesifikkeMetadata().deleteBsmField(groupId, longFieldId);
		}

		// It is also possible to remove a whole group:
		// mappe.getVirksomhetsspesifikkeMetadata().deleteBsmGroup(groupIdentfier);
		client.transaction()
				.save(mappe)
				.commit();

		// Make a query to fetch the Mappe
		QueryResponse<Mappe> queryResponse = client.query(Mappe.class, "id=@id", 1)
				.addQueryParam("@id", mappe.getId())
				.execute();

		mappe = queryResponse.getResults().iterator().next();

		//Print the new VirksomhetsspesifikkeMetadata
		groupsMap = mappe.getVirksomhetsspesifikkeMetadata();

		for (String gId : groupsMap.keySet()) {

			BsmFieldsMap fieldsMap = mappe.getVirksomhetsspesifikkeMetadata().get(gId);

			for (String fieldId : fieldsMap.keySet()) {

				BsmFieldValues values = fieldsMap.get(fieldId);
				print(
						"    GroupId=%s, FieldId=%s, ValueType=%s, Values=%s", gId, fieldId, values.getType(),
						values.getValues());
			}
		}

		print("End of Business-specific metadata example");
	}

	private static void print(String message, Object... parameters) {

		System.out.println(String.format(message, parameters));
	}

	private static void print() {

		System.out.println();
	}

	public static class Options {

		private final String idpServerAddress;

		private final String clientId;

		private final String clientSecret;

		private final String username;

		private final String password;

		private final String serverAddress;

		private final String certificatePath;

		private final String certificatePass;

//		private final String testDoc;

		public Options(
				String idpServerAddress, String clientId, String clientSecret, String username, String password,
				String serverAddress, String certificatePath, String certificatePass, String testDoc) {

			validateIsPropertySet("idpServerAddress", "idpServerAddress");
			validateIsPropertySet("clientId", clientId);
			validateIsPropertySet("clientSecret", clientSecret);
			validateIsPropertySet("username", username);
			validateIsPropertySet("password", password);
			validateIsPropertySet("serverAddress", serverAddress);
			validateIsPropertySet("testDoc", testDoc);

			this.idpServerAddress = idpServerAddress;
			this.clientId = clientId;
			this.clientSecret = clientSecret;
			this.username = username;
			this.password = password;
			this.serverAddress = serverAddress;
			this.certificatePath = certificatePath;
			this.certificatePass = certificatePass;
//			this.testDoc = testDoc;
		}

		public String getIdpServerAddress() {

			return idpServerAddress;
		}

		public String getClientId() {

			return clientId;
		}

		public String getClientSecret() {

			return clientSecret;
		}

		public String getUsername() {

			return username;
		}

		public String getPassword() {

			return password;
		}

		public String getServerAddress() {

			return serverAddress;
		}

		public String getCertificatePath() {

			return certificatePath;
		}

		public String getCertificatePass() {

			return certificatePass;
		}

		public String getTestDoc() {

			return testDoc;
		}

		private void validateIsPropertySet(String property, String value) {

			if (value == null || value.trim().isEmpty()) {

				throw new IllegalArgumentException(String.format("%s is required. Got %s", property, value));
			}
		}
	}
}