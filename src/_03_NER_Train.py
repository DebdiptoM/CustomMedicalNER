# Setting up the pipeline and entity recognizer.
import spacy
nlp = spacy.blank('en')  # create blank Language class
print("Created blank 'en' model")
if 'ner' not in nlp.pipe_names:
    ner = nlp.create_pipe('ner')
    nlp.add_pipe(ner)
else:
    ner = nlp.get_pipe('ner')

# Add new entity labels to entity recognizer
LABEL=['B-geo','I-org','B-gpe']
for i in LABEL:
    ner.add_label(i)
# Inititalizing optimizer
model=None
if model  is None:
    optimizer = nlp.begin_training()
else:
    optimizer = nlp.entity.create_optimizer()

other_pipes = [pipe for pipe in nlp.pipe_names if pipe != 'ner']
with nlp.disable_pipes(*other_pipes):  # only train NER
    for itn in range(n_iter):
        random.shuffle(TRAIN_DATA)
        losses = {}
        batches = minibatch(TRAIN_DATA, 
                            size=compounding(4., 32., 1.001))
        for batch in batches:
            texts, annotations = zip(*batch) 
            # Updating the weights
            nlp.update(texts, annotations, sgd=optimizer, 
                       drop=0.35, losses=losses)
        print('Losses', losses)
            nlp.update(texts, annotations, sgd=optimizer, 
                       drop=0.35, losses=losses)
        print('Losses', losses)

# Save model 
if output_dir is not None:
    output_dir = Path(output_dir)
    if not output_dir.exists():
        output_dir.mkdir()
    nlp.meta['name'] = new_model_name  # rename model
    nlp.to_disk(output_dir)
    print("Saved model to", output_dir)
