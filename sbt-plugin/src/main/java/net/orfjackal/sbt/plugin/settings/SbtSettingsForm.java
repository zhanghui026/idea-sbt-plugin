// Copyright © 2010, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package net.orfjackal.sbt.plugin.settings;

import com.intellij.execution.ui.AlternativeJREPanel;
import com.intellij.openapi.util.io.FileUtil;
import net.miginfocom.swing.MigLayout;
import net.orfjackal.sbt.plugin.IO;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.event.*;
import java.io.File;

public class SbtSettingsForm {
    // org.jetbrains.idea.maven.project.MavenImportingSettingsForm

    private final JPanel root;
    private final JTextField applicationSbtLauncherJarPath = new JTextField();
    private final JTextField applicationVmParameters = new JTextField();
    private final JCheckBox useApplicationSettings = new JCheckBox();
    private final JTextField projectSbtLauncherJarPath = new JTextField();
    private final JTextField projectVmParameters = new JTextField();
    private JLabel projectSbtLauncherLabel = new JLabel("SBT launcher JAR file (sbt-launch.jar)");
    private JLabel projectVmParametersLabel = new JLabel("VM parameters");
    private AlternativeJREPanel jreChooser = new AlternativeJREPanel();

    public SbtSettingsForm() {

        JPanel projectSettings = new JPanel(new MigLayout("", "[grow]", "[nogrid]"));
        projectSettings.setBorder(BorderFactory.createTitledBorder("Project Settings"));
        {
            useApplicationSettings.setText("Use IDE Settings");
            useApplicationSettings.setMnemonic('U');
            useApplicationSettings.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    enableOrDisableProjectSettings();
                }
            });
            projectSettings.add(useApplicationSettings, "wrap");
        }
        {
            projectSbtLauncherLabel.setDisplayedMnemonic('C');
            projectSbtLauncherLabel.setLabelFor(applicationSbtLauncherJarPath);

            JButton browse = new JButton("...");
            browse.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    browseForSbtLauncherJar(SbtSettingsForm.this.projectSbtLauncherJarPath);
                }
            });

            projectSettings.add(projectSbtLauncherLabel, "wrap");
            projectSettings.add(projectSbtLauncherJarPath, "growx");
            projectSettings.add(browse, "wrap");
        }
        {
            projectVmParametersLabel.setDisplayedMnemonic('M');
            projectVmParametersLabel.setLabelFor(projectVmParameters);
            projectSettings.add(projectVmParametersLabel, "wrap");
            projectSettings.add(projectVmParameters, "growx");
        }

        JPanel ideSettings = new JPanel(new MigLayout("", "[grow]", "[nogrid]"));
        ideSettings.setBorder(BorderFactory.createTitledBorder("IDE Settings"));
        {
            JLabel label = new JLabel("SBT launcher JAR file (sbt-launch.jar)");
            label.setDisplayedMnemonic('L');
            label.setLabelFor(applicationSbtLauncherJarPath);

            JButton browse = new JButton("...");
            browse.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    browseForSbtLauncherJar(SbtSettingsForm.this.applicationSbtLauncherJarPath);
                }
            });

            ideSettings.add(label, "wrap");
            ideSettings.add(applicationSbtLauncherJarPath, "growx");
            ideSettings.add(browse, "wrap");
        }
        {
            JLabel label = new JLabel("VM parameters");
            label.setDisplayedMnemonic('V');
            label.setLabelFor(applicationVmParameters);
            ideSettings.add(label, "wrap");
            ideSettings.add(applicationVmParameters, "wrap, growx");
            ideSettings.add(jreChooser, "growx");
        }

        root = new JPanel(new MigLayout("wrap 1", "[grow]"));
        root.add(projectSettings, "grow");
        root.add(ideSettings, "grow");
        enableOrDisableProjectSettings();
    }

    private void enableOrDisableProjectSettings() {
        boolean enable = !useApplicationSettings.isSelected();
        projectSbtLauncherLabel.setEnabled(enable);
        projectSbtLauncherJarPath.setEnabled(enable);
        projectVmParameters.setEnabled(enable);
        projectVmParametersLabel.setEnabled(enable);
    }

    private void browseForSbtLauncherJar(JTextField launcherJarTextField) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileFilter() {
            public boolean accept(File file) {
                return file.isDirectory() ||
                        file.getName().toLowerCase().endsWith(".jar");
            }

            public String getDescription() {
                return "JAR files (*.jar)";
            }
        });

        File oldValue = new File(launcherJarTextField.getText());
        chooser.setCurrentDirectory(oldValue);
        chooser.setSelectedFile(oldValue);

        int result = chooser.showOpenDialog(root);
        if (result == JFileChooser.APPROVE_OPTION) {
            launcherJarTextField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    public JComponent createComponent() {
        return root;
    }

    public boolean isModified(SbtProjectSettings projectSettings, SbtApplicationSettings applicationSettings) {
        SbtProjectSettings currentProj = new SbtProjectSettings();
        SbtApplicationSettings currentApp = new SbtApplicationSettings();
        copyTo(currentProj, currentApp);
        return !currentProj.equals(projectSettings) ||
                !currentApp.equals(applicationSettings);
    }

    public void copyTo(SbtProjectSettings projectSettings, SbtApplicationSettings applicationSettings) {
        projectSettings.setUseApplicationSettings(useApplicationSettings.isSelected());
        projectSettings.setSbtLauncherJarPath(FileUtil.toSystemIndependentName(projectSbtLauncherJarPath.getText()));
        projectSettings.setSbtLauncherVmParameters(projectVmParameters.getText());
        applicationSettings.setSbtLauncherJarPath(FileUtil.toSystemIndependentName(applicationSbtLauncherJarPath.getText()));
        applicationSettings.setSbtLauncherVmParameters(applicationVmParameters.getText());
        applicationSettings.setUseCustomJdk(jreChooser.isPathEnabled());
        applicationSettings.setJdkHome(jreChooser.getPath());
        enableOrDisableProjectSettings();
    }

    public void copyFrom(SbtProjectSettings projectSettings, SbtApplicationSettings applicationSettings) {
        projectSbtLauncherJarPath.setText(FileUtil.toSystemDependentName(IO.absolutePath(projectSettings.getSbtLauncherJarPath())));
        projectVmParameters.setText(projectSettings.getSbtLauncherVmParameters());
        useApplicationSettings.setSelected(projectSettings.isUseApplicationSettings());
        applicationSbtLauncherJarPath.setText(FileUtil.toSystemDependentName(IO.absolutePath(applicationSettings.getSbtLauncherJarPath())));
        applicationVmParameters.setText(applicationSettings.getSbtLauncherVmParameters());
        jreChooser.init(applicationSettings.getJdkHome(), applicationSettings.isUseCustomJdk());
    }

    public static void main(String[] args) {
        SbtSettingsForm form = new SbtSettingsForm();
        form.copyFrom(new SbtProjectSettings(), new SbtApplicationSettings());

        JFrame frame = new JFrame("Test: SbtSettingsForm");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(form.createComponent());
        frame.setSize(600, 600);
        frame.setLocation(500, 300);
        frame.setVisible(true);
    }
}
