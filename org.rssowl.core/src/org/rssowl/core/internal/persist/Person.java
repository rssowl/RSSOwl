/*   **********************************************************************  **
 **   Copyright notice                                                       **
 **                                                                          **
 **   (c) 2005-2009 RSSOwl Development Team                                  **
 **   http://www.rssowl.org/                                                 **
 **                                                                          **
 **   All rights reserved                                                    **
 **                                                                          **
 **   This program and the accompanying materials are made available under   **
 **   the terms of the Eclipse Public License v1.0 which accompanies this    **
 **   distribution, and is available at:                                     **
 **   http://www.rssowl.org/legal/epl-v10.html                               **
 **                                                                          **
 **   A copy is found in the file epl-v10.html and important notices to the  **
 **   license from the team is found in the textfile LICENSE.txt distributed **
 **   in this package.                                                       **
 **                                                                          **
 **   This copyright notice MUST APPEAR in all copies of the file!           **
 **                                                                          **
 **   Contributors:                                                          **
 **     RSSOwl Development Team - initial API and implementation             **
 **                                                                          **
 **  **********************************************************************  */

package org.rssowl.core.internal.persist;

import org.eclipse.core.runtime.Assert;
import org.rssowl.core.persist.IPerson;
import org.rssowl.core.persist.reference.PersonReference;
import org.rssowl.core.util.MergeUtils;

import java.net.URI;

/**
 * Each Feed or News may have a Person related as Author.
 *
 * @author bpasero
 */
public class Person extends AbstractEntity implements IPerson {
  private String fName;
  private String fUri;
  private String fEmail;

  /**
   * Creates a new Person Type with the given ID.
   *
   * @param id The unique ID of this person.
   */
  public Person(Long id) {
    super(id);
  }

  /**
   * Default constructor for deserialization
   */
  protected Person() {
  // As per javadoc
  }

  public Person(IPerson author) {
    synchronized (author) {
      setName(author.getName());
      setUri(author.getUri());
      setEmail(author.getEmail());
    }
  }

  /*
   * @see org.rssowl.core.model.types.IPerson#setName(java.lang.String)
   */
  public synchronized void setName(String name) {
    fName = name;
  }

  /*
   * @see org.rssowl.core.model.types.IPerson#setUri(java.lang.String)
   */
  public synchronized void setUri(URI uri) {
    fUri = getURIText(uri);
  }

  /*
   * @see org.rssowl.core.model.types.IPerson#setEmail(java.lang.String)
   */
  public synchronized void setEmail(URI email) {
    fEmail = getURIText(email);
  }

  /*
   * @see org.rssowl.core.model.types.IPerson#getName()
   */
  public synchronized String getName() {
    return fName;
  }

  /*
   * @see org.rssowl.core.model.types.IPerson#getUri()
   */
  public synchronized URI getUri() {
    return createURI(fUri);
  }

  /*
   * @see org.rssowl.core.model.types.IPerson#getEmail()
   */
  public synchronized URI getEmail() {
    return createURI(fEmail);
  }

  /*
   * @see org.rssowl.core.persist.MergeCapable#merge(java.lang.Object)
   */
  public synchronized MergeResult merge(IPerson objectToMerge) {
    Assert.isNotNull(objectToMerge);
    synchronized (objectToMerge) {
      boolean updated = !isSimpleFieldsEqual(objectToMerge);
      fName = objectToMerge.getName();
      setUri(objectToMerge.getUri());
      setEmail(objectToMerge.getEmail());
      ComplexMergeResult<?> mergeResult = MergeUtils.mergeProperties(this, objectToMerge);
      if (updated || mergeResult.isStructuralChange())
        mergeResult.addUpdatedObject(this);

      return mergeResult;
    }
  }

  private boolean isSimpleFieldsEqual(IPerson person) {
    return MergeUtils.equals(fName, person.getName()) &&
        MergeUtils.equals(getUri(), person.getUri()) &&
        MergeUtils.equals(getEmail(), person.getEmail());
  }

  /*
   * @see org.rssowl.core.persist.IEntity#toReference()
   */
  public PersonReference toReference() {
    return new PersonReference(getIdAsPrimitive());
  }

  /**
   * Compare the given type with this type for identity.
   *
   * @param person to be compared.
   * @return whether this object and <code>person</code> are identical. It
   * compares all the fields.
   */
  public synchronized boolean isIdentical(IPerson person) {
    if (this == person)
      return true;

    if (!(person instanceof Person))
      return false;

    synchronized (person) {
      Person p = (Person) person;

      return (getId() == null ? p.getId() == null : getId().equals(p.getId()))
          && (fName == null ? p.fName == null : fName.equals(p.fName))
          && (getUri() == null ? p.getUri() == null : getUri().toString().equals(p.getUri().toString()))
          && (getEmail() == null ? p.getEmail() == null : getEmail().equals(p.getEmail())) &&
          (getProperties() == null ? p.getProperties() == null : getProperties().equals(p.getProperties()));
    }
  }

  /*
   * @see org.rssowl.core.internal.persist.AbstractEntity#toString()
   */
  @Override
  public synchronized String toString() {
    return super.toString() + "Name = " + fName + ", URI = " + fUri + ", EMail = " + fEmail + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
  }
}