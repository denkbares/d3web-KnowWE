package de.d3web.tirex.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.html.HTMLEditorKit;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import de.d3web.kernel.domainModel.KnowledgeBase;
import de.d3web.kernel.domainModel.answers.AnswerChoice;
import de.d3web.kernel.domainModel.qasets.Question;
import de.d3web.tirex.core.OriginalMatchAndStrategy;
import de.d3web.tirex.core.TiRexFileReader;
import de.d3web.tirex.core.TiRexInterpreter;
import de.d3web.tirex.core.TiRexSettings;
import de.d3web.tirex.core.TiRexUtilities;
import de.d3web.tirex.core.extractionStrategies.DirectMatch;
import de.d3web.tirex.core.extractionStrategies.EditDistanceMatch;
import de.d3web.tirex.core.extractionStrategies.ExtractionStrategy;
import de.d3web.tirex.core.extractionStrategies.NumericalRegexMatch;
import de.d3web.tirex.core.extractionStrategies.StemmingMatch;
import de.d3web.tirex.core.extractionStrategies.SynonymDirectMatch;
import de.d3web.tirex.core.extractionStrategies.SynonymWithEditDistanceMatch;

/**
 * A frontend to test the TiRex API. I recommend reading the
 * "extractKnowledge()" method to see possible ways of using TiRex.
 * 
 * @author Dmitrij Frese
 * @date 02/2008
 */
public class TiRexPanel extends JPanel {

	private JFileChooser jfc;

	private JButton browseForWikiButton;

	private JButton browseForKBaseButton;

	private JButton browseForRegexButton;

	private JButton browseForSynonymsButton;

	private JSlider minimumMatchSlider;

	private JTextField wikiPathTextField;

	private JTextField kBasePathTextField;

	private JTextField regexPathTextField;

	private JTextField synonymsPathTextField;

	private JTabbedPane resultTabbedPane;

	private JButton parseButton;

	private JButton cancelButton;

	private JTextArea wikiFileTextArea;

	private JTextArea extractedBlockTextArea;

	private JEditorPane extractedBlockEditorPane;

	private JTextArea extractedKnowledgeTextArea;

	private JTextArea knowledgebaseInfoTextArea;

	private KnowledgeBase kb;

	private final static int TEXT_AREA_ROWS = 40;

	public TiRexPanel() {
		FormLayout formLayout = new FormLayout(
				"7dlu:nogrow, pref:nogrow, 3dlu:nogrow, 260dlu:grow, "
						+ "3dlu:nogrow, 60dlu:nogrow, 3dlu:nogrow,"
						+ " 60dlu:nogrow, 7dlu:nogrow",
				"6dlu:nogrow, pref:nogrow, 3dlu:nogrow, pref:nogrow, 3dlu:nogrow, "
						+ "pref:nogrow, 3dlu:nogrow, pref:nogrow, 6dlu:nogrow, "
						+ "1dlu:nogrow, 3dlu:nogrow, pref:nogrow, 3dlu:nogrow, "
						+ "1dlu:nogrow, 6dlu:nogrow, 260dlu:grow, 6dlu:nogrow, "
						+ "pref:nogrow, 6dlu:nogrow");
		setLayout(formLayout);

		CellConstraints cc = new CellConstraints();

		add(new JLabel("Wiki-File:"), cc.xywh(2, 2, 1, 1,
				CellConstraints.RIGHT, CellConstraints.CENTER));
		add(getWikiPathTextField(), cc.xywh(4, 2, 3, 1));
		add(getBrowseForWikiButton(), cc.xy(8, 2));

		add(new JLabel("Knowledgebase:"), cc.xywh(2, 4, 1, 1,
				CellConstraints.RIGHT, CellConstraints.CENTER));
		add(getKBasePathTextField(), cc.xywh(4, 4, 3, 1));
		add(getBrowseForKBaseButton(), cc.xy(8, 4));

		add(new JLabel("Regex-File:"), cc.xywh(2, 6, 1, 1,
				CellConstraints.RIGHT, CellConstraints.CENTER));
		add(getRegexPathTextField(), cc.xywh(4, 6, 3, 1));
		add(getBrowseForRegexButton(), cc.xy(8, 6));

		add(new JLabel("Synonym-File:"), cc.xywh(2, 8, 1, 1,
				CellConstraints.RIGHT, CellConstraints.CENTER));
		add(getSynonymsPathTextField(), cc.xywh(4, 8, 3, 1));
		add(getBrowseForSynonymsButton(), cc.xy(8, 8));

		JLabel blank = new JLabel(" ");
		blank.setOpaque(true);
		blank.setBackground(Color.WHITE);
		add(blank, cc.xywh(2, 10, 7, 1));

		add(new JLabel("Minimum match %:"), cc.xywh(2, 12, 1, 1,
				CellConstraints.RIGHT, CellConstraints.CENTER));
		add(getMinimumMatchJSlider(), cc.xywh(4, 12, 3, 1));

		JLabel blank2 = new JLabel(" ");
		blank2.setOpaque(true);
		blank2.setBackground(Color.WHITE);
		add(blank2, cc.xywh(2, 14, 7, 1));

		try {
			add(getResultTabbedPane(), cc.xywh(2, 16, 7, 1));
		} catch (IOException e) {
			e.printStackTrace();
		}

		add(getParseButton(), cc.xy(6, 18));
		add(getCancelButton(), cc.xy(8, 18));
	}

	private Component getResultTabbedPane() throws IOException {
		if (resultTabbedPane == null) {
			resultTabbedPane = new JTabbedPane();
			resultTabbedPane.addTab("Wiki-Page", new JScrollPane(
					getWikiFileTextArea()));
			resultTabbedPane.addTab("Extracted Block", new JScrollPane(
					getExtractedBlockTextArea()));
			resultTabbedPane.addTab("Formatted Extracted Block",
					new JScrollPane(getExtractedBlockEditorPane()));
			resultTabbedPane.addTab("POS-Tagged Extracted Block",
					new JScrollPane());
			resultTabbedPane.addTab("Extracted Knowledge", new JScrollPane(
					getExtractedKnowledgeTextArea()));
			resultTabbedPane.addTab("Knowledgebase Information",
					new JScrollPane(getKnowledgebaseInfoTextArea()));
		}

		return resultTabbedPane;
	}

	private Component getMinimumMatchJSlider() {
		if (minimumMatchSlider == null) {
			minimumMatchSlider = new JSlider();
			minimumMatchSlider.setMinorTickSpacing(1);
			minimumMatchSlider.setMajorTickSpacing(5);
			minimumMatchSlider.setSnapToTicks(true);
			minimumMatchSlider.setPaintTicks(true);
			minimumMatchSlider.setPaintLabels(true);

			minimumMatchSlider.setValue((int) TiRexSettings.getInstance()
					.getMinimumMatchPercentage());

			minimumMatchSlider.addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent e) {
					int value = ((JSlider) e.getSource()).getValue();
					TiRexSettings.getInstance()
							.setMinimumMatchPercentage(value);
				}
			});
		}

		return minimumMatchSlider;
	}

	private JTextArea getWikiFileTextArea() {
		if (wikiFileTextArea == null) {
			wikiFileTextArea = new JTextArea("No file parsed yet.");
			wikiFileTextArea.setRows(TEXT_AREA_ROWS);
		}

		return wikiFileTextArea;
	}

	private JTextArea getExtractedBlockTextArea() {
		if (extractedBlockTextArea == null) {
			extractedBlockTextArea = new JTextArea();
			extractedBlockTextArea.setText("No file parsed yet.");
			extractedBlockTextArea.setRows(TEXT_AREA_ROWS);
		}

		return extractedBlockTextArea;
	}

	private JEditorPane getExtractedBlockEditorPane() throws IOException {
		if (extractedBlockEditorPane == null) {
			extractedBlockEditorPane = new JEditorPane();
			extractedBlockEditorPane.setEditorKit(new HTMLEditorKit());
			extractedBlockEditorPane.setText("No file parsed yet.");
		}

		return extractedBlockEditorPane;
	}

	private JTextArea getExtractedKnowledgeTextArea() {
		if (extractedKnowledgeTextArea == null) {
			extractedKnowledgeTextArea = new JTextArea("No file parsed yet.");
			extractedKnowledgeTextArea.setRows(TEXT_AREA_ROWS);
		}

		return extractedKnowledgeTextArea;
	}

	private JTextArea getKnowledgebaseInfoTextArea() {
		if (knowledgebaseInfoTextArea == null) {
			knowledgebaseInfoTextArea = new JTextArea("No file parsed yet.");
			knowledgebaseInfoTextArea.setRows(TEXT_AREA_ROWS);
		}

		return knowledgebaseInfoTextArea;
	}

	private JTextField getWikiPathTextField() {
		if (wikiPathTextField == null) {
			wikiPathTextField = new JTextField("");
			wikiPathTextField
					.setToolTipText("Path to the Wiki-File, which is to be parsed.");
			wikiPathTextField.setEditable(false);
		}

		return wikiPathTextField;
	}

	private JButton getBrowseForWikiButton() {
		if (browseForWikiButton == null) {
			browseForWikiButton = new JButton("Browse...");
			browseForWikiButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int state = getJFC().showOpenDialog(TiRexPanel.this);

					if (state == JFileChooser.APPROVE_OPTION) {
						getWikiPathTextField().setText(
								getJFC().getSelectedFile().getPath());

						try {
							TiRexFileReader
									.getInstance()
									.setWikiFile(
											TiRexUtilities
													.getInstance()
													.getReaderAsString(
															new FileReader(
																	getJFC()
																			.getSelectedFile())));
						} catch (IOException e1) {
							e1.printStackTrace();
						}

						Preferences.userNodeForPackage(getClass()).put(
								"TiRexSettingsPath",
								getJFC().getCurrentDirectory().getPath());

					}
				}
			});
		}

		return browseForWikiButton;
	}

	private JTextField getKBasePathTextField() {
		if (kBasePathTextField == null) {
			kBasePathTextField = new JTextField("");
			kBasePathTextField
					.setToolTipText("Path to the Knowledgebase, which is to be parsed.");
			kBasePathTextField.setEditable(false);
		}

		return kBasePathTextField;
	}

	private JButton getBrowseForKBaseButton() {
		if (browseForKBaseButton == null) {
			browseForKBaseButton = new JButton("Browse...");
			browseForKBaseButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int state = getJFC().showOpenDialog(TiRexPanel.this);

					if (state == JFileChooser.APPROVE_OPTION) {
						getKBasePathTextField().setText(
								getJFC().getSelectedFile().getPath());

						try {
							TiRexFileReader
									.getInstance()
									.setKnowledgeBase(
											TiRexFileReader
													.getInstance()
													.loadKnowledgebase(
															getJFC()
																	.getSelectedFile()));
						} catch (MalformedURLException e1) {
							e1.printStackTrace();
						}

						Preferences.userNodeForPackage(getClass()).put(
								"TiRexSettingsPath",
								getJFC().getCurrentDirectory().getPath());
					}
				}
			});
		}

		return browseForKBaseButton;
	}

	private JTextField getRegexPathTextField() {
		if (regexPathTextField == null) {
			regexPathTextField = new JTextField("");
			regexPathTextField
					.setToolTipText("Path to the Settings for the Regular expressions, which are to be parsed.");
			regexPathTextField.setEditable(false);
		}

		return regexPathTextField;
	}

	private JButton getBrowseForRegexButton() {
		if (browseForRegexButton == null) {
			browseForRegexButton = new JButton("Browse...");
			browseForRegexButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int state = getJFC().showOpenDialog(TiRexPanel.this);

					if (state == JFileChooser.APPROVE_OPTION) {
						getRegexPathTextField().setText(
								getJFC().getSelectedFile().getPath());

						try {
							TiRexFileReader
									.getInstance()
									.setRegexKnofficePairsFile(
											TiRexUtilities
													.getInstance()
													.getReaderAsString(
															new FileReader(
																	getJFC()
																			.getSelectedFile())));
						} catch (IOException e1) {
							e1.printStackTrace();
						}

						Preferences.userNodeForPackage(getClass()).put(
								"TiRexSettingsPath",
								getJFC().getCurrentDirectory().getPath());
					}
				}
			});
		}

		return browseForRegexButton;
	}

	private JTextField getSynonymsPathTextField() {
		if (synonymsPathTextField == null) {
			synonymsPathTextField = new JTextField("");
			synonymsPathTextField
					.setToolTipText("Path to the sets of synonyms, which are to be parsed.");
			synonymsPathTextField.setEditable(false);
		}

		return synonymsPathTextField;
	}

	private JButton getBrowseForSynonymsButton() {
		if (browseForSynonymsButton == null) {
			browseForSynonymsButton = new JButton("Browse...");
			browseForSynonymsButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int state = getJFC().showOpenDialog(TiRexPanel.this);

					if (state == JFileChooser.APPROVE_OPTION) {
						getSynonymsPathTextField().setText(
								getJFC().getSelectedFile().getPath());

						try {
							TiRexFileReader
									.getInstance()
									.setSynonymSetsFile(
											TiRexUtilities
													.getInstance()
													.getReaderAsString(
															new FileReader(
																	getJFC()
																			.getSelectedFile())));
						} catch (IOException e1) {
							e1.printStackTrace();
						}

						Preferences.userNodeForPackage(getClass()).put(
								"TiRexSettingsPath",
								getJFC().getCurrentDirectory().getPath());
					}
				}
			});
		}

		return browseForSynonymsButton;
	}

	private JFileChooser getJFC() {
		if (jfc == null) {
			jfc = new JFileChooser();

			String path = Preferences.userNodeForPackage(getClass()).get(
					"TiRexSettingsPath", System.getProperty("user.home"));
			jfc.setCurrentDirectory(new File(path));
		}

		return jfc;
	}

	private JButton getParseButton() {
		if (parseButton == null) {
			parseButton = new JButton("Parse");
			parseButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					try {
						getKBaseInfo();
						getWikiPage();
						extractKnowledgeBlock();
						extractKnowledge();

						resetCarets();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}

		return parseButton;
	}

	protected void resetCarets() {
		getWikiFileTextArea().setCaretPosition(0);
		getExtractedBlockTextArea().setCaretPosition(0);
		getExtractedKnowledgeTextArea().setCaretPosition(0);
		getKnowledgebaseInfoTextArea().setCaretPosition(0);
	}

	protected void extractKnowledge() throws IOException {
		if (kb != null) {
			StringBuffer knowledge = new StringBuffer();
			StringBuffer html = new StringBuffer();

			String[] blockSegments = getExtractedBlockTextArea().getText()
					.split("\n");
			Collection<String> preprocessedBlockSegments = new ArrayList<String>();
			for (String segment : blockSegments) {
				preprocessedBlockSegments.addAll(TiRexUtilities.getInstance()
						.convertArrayToCollection(segment.split("(A|U)ND")));
			}

			Collection<ExtractionStrategy> questionStrategies = new ArrayList<ExtractionStrategy>();
			questionStrategies.add(DirectMatch.getInstance());
			questionStrategies.add(EditDistanceMatch.getInstance());
			questionStrategies.add(SynonymDirectMatch.getInstance());
			questionStrategies.add(SynonymWithEditDistanceMatch.getInstance());

			Collection<ExtractionStrategy> answerStrategies = new ArrayList<ExtractionStrategy>();
			answerStrategies.add(DirectMatch.getInstance());
			answerStrategies.add(SynonymDirectMatch.getInstance());
			answerStrategies.add(StemmingMatch.getInstance());
			answerStrategies.add(EditDistanceMatch.getInstance());
			answerStrategies.add(SynonymWithEditDistanceMatch.getInstance());
			answerStrategies.add(NumericalRegexMatch.getInstance());
			// answerStrategies.addAll(questionStrategies);
			// answerStrategies.add(StemmingMatch.getInstance());
			// answerStrategies.add(NumericalRegexMatch.getInstance());

			// for (String segment : preprocessedBlockSegments) {
			// OriginalMatchAndStrategy omas = TiRexInterpreter.getInstance()
			// .extractDiagnosis(kb, segment, answerStrategies);
			//
			// if (omas != null) {
			// knowledge.append(TiRexUtilities.getInstance()
			// .convertNumericalExpressionToKnOffice(
			// omas.getMatch(),
			// TiRexSettings.getInstance()
			// .getRegexKnofficePairs())
			// + "\n");
			//
			// html.append(segment.replaceAll(omas.getMatch(), omas
			// .getStrategy().getAnnotation().getPrefix()
			// + omas.getMatch()
			// + omas.getStrategy().getAnnotation().getSuffix())
			// + "<br>");
			// }
			// }

			for (String segment : preprocessedBlockSegments) {
				OriginalMatchAndStrategy omas = TiRexInterpreter.getInstance()
						.extractQuestion(kb, segment, questionStrategies);

				if (omas != null) {
					Question q = (Question) omas.getIDObject();

					OriginalMatchAndStrategy omas2 = TiRexInterpreter
							.getInstance().extractAnswer(q, segment, kb,
									answerStrategies);

					// String temp = segment.replaceAll(omas.getMatch(), omas
					// .getStrategy().getAnnotation().getPrefix()
					// + omas.getMatch()
					// + omas.getStrategy().getAnnotation().getSuffix())
					// + "<br>";

					String temp = segment.replaceAll(omas.getMatch(), omas
							.getStrategy().getAnnotation().getPrefix()
							+ omas.getMatch()
							+ omas.getStrategy().getAnnotation().getSuffix())
							+ "<br>";

					if (omas2 != null) {
						temp = temp.replaceAll(omas2.getMatch(), omas2
								.getStrategy().getAnnotation().getPrefix()
								+ omas2.getMatch()
								+ omas2.getStrategy().getAnnotation()
										.getSuffix());

						// knowledge
						// .append(omas.getMatch()
						// + " "
						// + TiRexUtilities
						// .getInstance()
						// .convertNumericalExpressionToKnOffice(
						// omas2.getMatch(),
						// TiRexSettings
						// .getInstance()
						// .getRegexKnofficePairs())
						// + "\n");

						knowledge
								.append("Die ["
										+ omas.getMatch()
										+ " <=> isQuestion::"
										+ ((Question) omas.getIDObject())
												.getText()
										+ "]"
										+ "] hat die ["
										+ TiRexUtilities
												.getInstance()
												.convertNumericalExpressionToKnOffice(
														omas2.getMatch(),
														TiRexSettings
																.getInstance()
																.getRegexKnofficePairs())
										+ " <=> isAnswer::"
										+ ((AnswerChoice) omas2.getIDObject())
												.getText() + "]"

										+ "\n");
					} else {
						knowledge.append(omas.getMatch() + "\n");
					}

					html.append(temp);
				} else {
					omas = TiRexInterpreter.getInstance().extractAnswer(null,
							segment, kb, answerStrategies);

					if (omas != null) {
						knowledge.append(TiRexUtilities.getInstance()
								.convertNumericalExpressionToKnOffice(
										omas.getMatch(),
										TiRexSettings.getInstance()
												.getRegexKnofficePairs())
								+ "\n");

						html.append(segment.replaceAll(omas.getMatch(), omas
								.getStrategy().getAnnotation().getPrefix()
								+ omas.getMatch()
								+ omas.getStrategy().getAnnotation()
										.getSuffix())
								+ "<br>");
					} else {
						omas = TiRexInterpreter.getInstance().extractDiagnosis(
								kb, segment, answerStrategies);

						if (omas != null) {
							knowledge.append(TiRexUtilities.getInstance()
									.convertNumericalExpressionToKnOffice(
											omas.getMatch(),
											TiRexSettings.getInstance()
													.getRegexKnofficePairs())
									+ "\n");

							html.append(segment.replaceAll(omas.getMatch(),
									omas.getStrategy().getAnnotation()
											.getPrefix()
											+ omas.getMatch()
											+ omas.getStrategy()
													.getAnnotation()
													.getSuffix())
									+ "<br>");
						}
					}
				}
			}

			getExtractedBlockEditorPane().setText(
					"<html><head></head><body><p>" + html
							+ "</p></body></html>");
			getExtractedKnowledgeTextArea().setText(knowledge.toString());
		} else {
			getExtractedBlockEditorPane().setText(
					"<html><head></head><body><p>"
							+ "The knowledgebase is not valid."
							+ "</p></body></html>");
			getExtractedKnowledgeTextArea().setText(
					"The knowledgebase is not valid.");
		}
	}

	protected void extractKnowledgeBlock() throws IOException {
		if (getWikiFileTextArea().getText() != null) {
			getExtractedBlockTextArea().setText(
					TiRexInterpreter.getInstance().getExtractedKnowledgeBlock(
							getWikiFileTextArea().getText()));
		}
	}

	protected void getWikiPage() throws Exception {
		Reader wikiFileReader = new FileReader(TiRexFileReader.getInstance()
				.getWikiFile());

		if (wikiFileReader != null) {
			getWikiFileTextArea().setText(
					TiRexUtilities.getInstance().getReaderAsString(
							wikiFileReader));
		} else {
			getWikiFileTextArea().setText("No Wiki-File specified.");
		}
	}

	protected void getKBaseInfo() throws Exception {
		kb = TiRexFileReader.getInstance().getKnowledgeBase();

		if (kb != null) {
			getKnowledgebaseInfoTextArea().setText(
					TiRexUtilities.getInstance()
							.getAllQuestionsAnswersAndDiagnosesAsString(kb,
									true));
		} else {
			getKnowledgebaseInfoTextArea().setText(
					"No Knowledgebase-File specified.");
		}
	}

	private JButton getCancelButton() {
		if (cancelButton == null) {
			cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					System.exit(0);
				}
			});
		}

		return cancelButton;
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame("TiRex");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.getContentPane().add(new TiRexPanel());

		frame.pack();
		frame.setVisible(true);
	}
}
